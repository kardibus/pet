package com.kardibus.pet.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.kardibus.pet.model.CompletionOptions
import com.kardibus.pet.model.Message
import com.kardibus.pet.model.ModelRequest
import com.kardibus.pet.model.ModelResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
    private @Value("\${yandex.api.key}") var yandexApiKey: String,
    private @Value("\${yandex.folder.id}") var folderId: String
) : Cache<Long, String>() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var delayGlobal = 0

    override fun sendMessage(): MutableMap<Long, MutableMap<Long, String>> {
        val result: MutableMap<Long, MutableMap<Long, String>> = HashMap()

        if (cache.isEmpty()) {
            return result
        }

        val chat = cache.entries.first()
        val keyMessage = chat.value.keys

//        if (keyMessage.size <= 1) {
//            return result
//        }

        keyMessage.toList().forEach { messageId ->
            val completionOptions = CompletionOptions(stream = false, temperature = 0.0, maxTokens = "2000")
            val systemMessage = Message(
                role = "system",
                text = "верни true если в тексте есть матершинные слова или текст оскорбляет, иначе верни false"
            )
            val userMessage = Message(role = "user", text = chat.value[messageId])

            // Create model request
            val modelRequest = ModelRequest(
                modelUri = "gpt://${folderId}/yandexgpt/latest",
                completionOptions = completionOptions,
                messages = listOf(systemMessage, userMessage)
            )

            val objectMapper = ObjectMapper()
            val json = objectMapper.writeValueAsString(modelRequest)

            Log.info(json)

            runBlocking { delay(delayGlobal.toLong()) }

            val response = try {
                webClient.post().uri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Api-Key ${yandexApiKey}")
                    //   .header("x-folder-id", "b1gh1vnfkg7ti0vn41di")
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .block()

            } catch (e: WebClientResponseException.Unauthorized) {
                logger.error("Unauthorized error: {}", e.message)
                null
            } catch (e: WebClientResponseException) {
                logger.error("WebClient error: {}", e.message)
                delayGlobal.plus(10000)
                null
            } catch (e: Exception) {
                logger.error("Unexpected error: {}", e.message)
                null
            }
            logger.info(response)
            response?.let {
                val modelResponse = Gson().fromJson(it, ModelResponse::class.java)
                if (!modelResponse.result.alternatives.isNullOrEmpty()) {
                    val messagesMap = mutableMapOf<Long, String>()
                    messagesMap[messageId] = modelResponse.result.alternatives[0].message.text.toString()
                    result[chat.key] = messagesMap
                }
            }

            // Удаляем сообщение из кэша только после успешной отправки
            response?.let {
                cache[chat.key]?.remove(messageId)
                delayGlobal.minus(10000)
            }
        }

        return result
    }
}
