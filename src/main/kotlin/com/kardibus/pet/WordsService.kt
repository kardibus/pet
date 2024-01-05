package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

val Log = LoggerFactory.getLogger(WordsService::class.java)

@Service
class WordsService(
    private var wordsRepository: WordsRepository,
    private val bot: Bot,
    private val webClient: WebClient,
    @Value("\${iam.token}") val iamToken: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var isWord: Boolean = true

    private var map = mapOf(0 to "мразь", 1 to "хуесос", 2 to "уебок", 3 to "пидор")

    init {
        GlobalScope.launch { start() }
    }

    fun start() {
        bot.onMessage { msg ->
            isWord = true
            if (msg.text != null) {
                //     val words: List<String> = msg.text?.split(" ", ".", ",", "?", "!")!!.toList()

                // Create completion options
                var completionOptions = CompletionOptions(stream = false, temperature = 0.0, maxTokens = "1000")


                val systemMessage = Message(
                    role = "system",
                    text = "Если есть в тексте ругательные слова или текст имеет оскорбляющий смысл верни только true, иначе возвращай только false"
                )
                val userMessage = Message(role = "user", text = "${msg.text}")

                // Create model request
                val modelRequest = ModelRequest(
                    modelUri = "gpt://b1g28ivuvukiq10nl0ip/yandexgpt-lite",
                    completionOptions = completionOptions,
                    messages = listOf(systemMessage, userMessage)
                )

                val objectMapper = ObjectMapper()
                val json = objectMapper.writeValueAsString(modelRequest)

                Log.info(json)

                val response =
                    webClient.post().uri("https://llm.api.cloud.yandex.net/foundationModels/v1/completion")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer $iamToken")
                        .header("x-folder-id", "b1g28ivuvukiq10nl0ip")
                        .body(BodyInserters.fromValue(json))
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .doOnError { throwable ->
                            if (throwable is WebClientResponseException) {
                                // Обработка ошибок HTTP
                                val statusCode = throwable.statusCode
                                val responseBody = throwable.responseBodyAsString
                                Log.error("HTTP Error: $statusCode, Response: $responseBody")
                            } else {
                                // Обработка других исключений
                                Log.error("Error: ${throwable.message}")
                            }
                        }
                        .block()

                val gson = Gson().fromJson(response, ModelResponse::class.java)

                Log.info(gson.toString())

                if (gson!!.result.alternatives.first().message.text.lowercase() == "true") {
                    var random = (0..3).random()
                    bot.sendMessage(
                        msg.chat.id.toChatId(),
                        replyToMessageId = msg.messageId,
                        text = "у нас нельзя оскорблять или матерится в чате ${map.get(random)}  \uD83D\uDE19"
                    )
                }

//                for (word in words) {
//                    if (wordsRepository.findByWordOutInt(word.lowercase()) > 0 && isWord && word.isNotEmpty()) {
//
//                        logger.info("$msg")
//                        logger.info("${msg.from!!.first_name} ${msg.from!!.lastName}")
//                        var random = (0..3).random()
//                        bot.sendMessage(
//                            msg.chat.id.toChatId(),
//                            replyToMessageId = msg.messageId,
//                            text = "у нас нельзя матерится в чате ${map.get(random)}  \uD83D\uDE19"
//                        )
//                        isWord = false
//                    }
//                }
            }

            if (!msg.newChatMembers.isNullOrEmpty()) {
                bot.sendMessage(
                    msg.chat.id.toChatId(),
                    "Приветствуем тебя в чате ${msg.newChatMembers.first().first_name} ${msg.newChatMembers.first().lastName}"
                )
            }

            if (msg.leftChatMember?.id != null) {
                bot.sendMessage(
                    msg.chat.id.toChatId(),
                    "Прощай ${msg.leftChatMember?.first_name} ${msg.leftChatMember?.lastName}"
                )
            }
        }

        bot.start()
    }
}