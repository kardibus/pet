package com.kardibus.pet.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.kardibus.pet.model.MessageDTO
import com.kardibus.pet.model.ModelRequestSber
import com.kardibus.pet.model.ModelResponseSber
import com.kardibus.pet.model.ModelResponseTokenSber
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

private val logger = LoggerFactory.getLogger(SenderMessageSberImpl::class.java)

@Service
@Qualifier("sber")
class SenderMessageSberImpl(
    private val webClient: WebClient,
    private @Value("\${sber.secret}") val sberSecret: String
) : Cache<Long, String>() {
    private var token: String = ""

    override fun sendMessage(): MutableMap<Long, MutableMap<Long, String>> {
        val result: MutableMap<Long, MutableMap<Long, String>> = HashMap()

        if (cache.isEmpty()) {
            return result
        }

        if (token.isNullOrEmpty()) {
            updateToken()
        }

        for ((chatId, messages) in cache) {
            val messagesMap = mutableMapOf<Long, String>()

            messages.keys.forEach { messageId ->
                val systemMessage = MessageDTO(
                    role = "system",
                    content = "верни true если в тексте есть матершинные слова или в тексте есть оскорбления, иначе верни false и возвращай false если в тексте хвалят или текст имеет положительный или нейтральный характер"
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

                runBlocking { delay(delayGlobal.toLong()) }

                val response = try {
                    webClient.post()
                        .uri("https://gigachat.devices.sberbank.ru/api/v1/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer $token")
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

                response?.let {
                    val modelResponse = Gson().fromJson(it, ModelResponseSber::class.java)
                    if (!modelResponse.choices.isNullOrEmpty()) {
                        val choice = modelResponse.choices[0]
                        messagesMap[messageId] = choice.message.content
                        cache[chatId]?.remove(messageId)
                        if (delayGlobal >= 11000) {
                            delayGlobal.minus(10000)
                        }
                    }
                }
            }
            result[chatId] = messagesMap
        }
        return result
    }

    fun updateToken() {
        token = requestToken()
    }

    private fun requestToken(): String {
        val scope = "GIGACHAT_API_PERS"
        var token = ""

        val response = try {
            webClient.post()
                .uri("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("RqUID", "eff806e2-f981-4c8c-86ec-79991490c66e")
                .header(HttpHeaders.AUTHORIZATION, "Basic $sberSecret")
                .body(BodyInserters.fromFormData("scope", scope))
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
            val modelResponse = Gson().fromJson(it, ModelResponseTokenSber::class.java)
            token = modelResponse.access_token
        }
        return token
    }
}
