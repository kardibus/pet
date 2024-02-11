package com.kardibus.pet.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.kardibus.pet.model.CompletionOptions
import com.kardibus.pet.model.Message
import com.kardibus.pet.model.ModelRequest
import com.kardibus.pet.model.ModelResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

val Log = LoggerFactory.getLogger(SenderMessage::class.java)

@Service
class SenderMessageYandexImpl(
    private val webClient: WebClient,
    @Value("\${iam.token}") val iamToken: String
): SenderMessage   {
    private val logger = LoggerFactory.getLogger(javaClass)

    private var cache: MutableMap<Long, MutableMap<Long, String>> = HashMap()

    fun addMessage(chatId: Long, messageId: Long, message: String) {
        cache.putIfAbsent(chatId, mutableMapOf())
        cache[chatId]?.put(messageId, message)
    }

     override fun sendMessage(): MutableMap<Long, MutableMap<Long, String>> {
        val result: MutableMap<Long, MutableMap<Long, String>> = HashMap()

        if (cache.isEmpty()) {
            return result
        }

        val chat = cache.entries.first()
        val keyMessage = chat.value.keys

        if (keyMessage.size <= 2) {
            return result
        }

        keyMessage.chunked(4).forEach { messageIdsChunk ->
            val completionOptions = CompletionOptions(stream = false, temperature = 0.0, maxTokens = "1000")
            val systemMessage = Message(
                role = "system",
                text = "верни true если в тексте есть матершинные слова или текст кого то оскорбляет иначе верни false"
            )
            val userMessages = messageIdsChunk.map { messageId ->
                Message(role = "user", text = chat.value[messageId])
            }

            // Create model request
            val modelRequest = ModelRequest(
                modelUri = "gpt://b1g28kivuvukiq10nl0ip/yandexgpt-lite",
                completionOptions = completionOptions,
                messages = listOf(systemMessage) + userMessages
            )

            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(modelRequest)

            Log.info(json)

            val response = try {
                webClient.post().uri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer $iamToken")
                    .header("x-folder-id", "b1g28ivuvukiq10nl0ip")
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .block()
            } catch (e: WebClientResponseException.Unauthorized) {
                logger.error("Unauthorized error: {}", e.message)
                null
            } catch (e: WebClientResponseException) {
                logger.error("WebClient error: {}", e.message)
                null
            } catch (e: Exception) {
                logger.error("Unexpected error: {}", e.message)
                null
            }

            response?.let {
                val modelResponse = Gson().fromJson(it, ModelResponse::class.java)
                if (!modelResponse.result.alternatives.isNullOrEmpty()) {
                    for ((chatId, messageId) in chat.value) {
                        val messagesMap = result.getOrPut(chatId) { mutableMapOf() }
                        messagesMap[messageId.toLong()] = modelResponse.result.alternatives[0].message.text.toString()
                    }
                }
            }

            // Удаляем сообщения из кэша только после успешной отправки
            response?.let {
                for (messageId in messageIdsChunk) {
                    cache[chat.key]?.remove(messageId)
                }
            }
        }

        return result
    }
}
