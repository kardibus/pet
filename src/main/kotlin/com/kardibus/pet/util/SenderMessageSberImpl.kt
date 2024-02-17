package com.kardibus.pet.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.kardibus.pet.model.CompletionOptions
import com.kardibus.pet.model.Message
import com.kardibus.pet.model.MessageDTO
import com.kardibus.pet.model.ModelRequestSber
import com.kardibus.pet.model.ModelResponse
import com.kardibus.pet.model.ModelResponseSber
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class SenderMessageSberImpl(
    private val webClient: WebClient,
    @Value("\${sber.key}") private val sberKey: String,
    @Value("\${sber.secret}") private val sberSecret: String
) : Cache<Long, String>() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendMessage(): MutableMap<Long, MutableMap<Long, String>> {
        val result: MutableMap<Long, MutableMap<Long, String>> = HashMap()

        if (cache.isEmpty()) {
            return result
        }

        for ((chatId, messages) in cache) {
            val messagesMap = mutableMapOf<Long, String>()

            messages.keys.forEach { messageId ->
                val completionOptions = CompletionOptions(stream = false, temperature = 0.0, maxTokens = "2000")
                val systemMessage = MessageDTO(
                    role = "system",
                    content = "верни true если в тексте есть матершинные слова или текст оскорбляет, иначе верни false"
                )
                val userMessage = MessageDTO(
                    role = "user",
                    content = messages[messageId] ?: ""
                )

                val modelRequest = ModelRequestSber(
                    model = "GigaChat:latest",
                    temperature = 1.0,
                    top_p = 0.1,
                    n = 1,
                    max_tokens = 1000,
                    repetition_penalty = 1.0,
                    stream = false,
                    update_interval = 0,
                    messages = listOf(systemMessage, userMessage)
                )

                val objectMapper = ObjectMapper()
                val json = objectMapper.writeValueAsString(modelRequest)

                val response = try {
                    webClient.post()
                        .uri("https://gigachat.devices.sberbank.ru/api/v1/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer $sberKey")
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
                    val modelResponse = objectMapper.readValue(it, ModelResponseSber::class.java)
                    if (!modelResponse.choices.isNullOrEmpty()) {
                        val choice = modelResponse.choices[0]
                        messagesMap[messageId] = choice.message.content
                        cache[chatId]?.remove(messageId)
                    }
                }
            }
            result[chatId] = messagesMap
        }

        return result
    }
}
