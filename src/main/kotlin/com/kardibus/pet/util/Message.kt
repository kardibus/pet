package com.kardibus.pet.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class Message(
    private val webClient: WebClient,
    @Value("\${iam.token}") val iamToken: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private var cashe = StringBuilder()

    fun addMessage(string: String) {
        cashe.append(string)
        cashe.append(" ")
    }

    fun startSendingMessages() {
        sendMessage()
    }

    private fun sendMessage() {
//        var completionOptions = CompletionOptions(stream = false, temperature = 0.0, maxTokens = "1000")
//
//
//        val systemMessage = Message(
//            role = "system",
//            text = "Верни true если в тексте есть сука"
//        )
//        val userMessage = Message(role = "user", text = "${msg}")
//
//        // Create model request
//        val modelRequest = ModelRequest(
//            modelUri = "gpt://b1g28ivuvukiq10nl0ip/yandexgpt-lite",
//            completionOptions = completionOptions,
//            messages = listOf(systemMessage, userMessage)
//        )
//
//        val objectMapper = ObjectMapper()
//        val json = objectMapper.writeValueAsString(modelRequest)
//
//        Log.info(json)
//
//        var response:String? =
//            try {
//               webClient.post().uri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
//                    .header("Content-Type", "application/json")
//                    .header("Authorization", "Bearer $iamToken")
//                    .header("x-folder-id", "b1g28ivuvukiq10nl0ip")
//                    .body(BodyInserters.fromValue(json))
//                    .retrieve()
//                    .bodyToMono(String::class.java).block()
//            }catch (e:WebClientResponseException)({
//                logger.error(e.message)
//            }).toString()
//
//        return Gson().fromJson(response, ModelResponse::class.java)
        if (cashe.length > 300) {
            logger.info(cashe.toString())
            cashe.clear()
        }
        logger.info(cashe.toString())
    }
}
