package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.kardibus.pet.model.Words
import com.kardibus.pet.util.SenderMessageSberImpl
import com.kardibus.pet.util.SenderMessageYandexImpl
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

val Log = LoggerFactory.getLogger(WordsService::class.java)

@Service
class WordsService(
    private var wordsRepository: WordsRepository,
    private val bot: Bot,
    private val senderMessageYandexImpl: SenderMessageYandexImpl,
    private val senderMessageSberImpl: SenderMessageSberImpl,
    private @Value("\${ya.enable}") var yaEnable: Boolean,
    private @Value("\${sber.enable}") var sberEnable: Boolean,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var isWord: Boolean = true
    private var isMadWord: Boolean = false
    private var map = mapOf(0 to "мразь", 1 to "хуесос", 2 to "уебок", 3 to "пидор")

    init {
        start()
    }

    fun start() {
        bot.onMessage { msg ->
            isWord = true
            isMadWord=false

            if (!isMadWord && yaEnable) {
                runBlocking {
                    senderMessageYandexImpl.addMessage(msg.chat.id, msg.messageId, msg.text.toString())
                    val result = senderMessageYandexImpl.sendMessage()
                    logger.info(result.toString())
                    result.forEach { (chatId, messagesMap) ->
                        messagesMap.forEach { (messageId, responseMessage) ->
                            var random = (0..3).random()
                            if (responseMessage == "true") {
                                try {
                                    isMadWord = responseMessage.toBoolean()
                                    bot.sendMessage(
                                        chatId.toLong().toChatId(),
                                        replyToMessageId = messageId,
                                        text = "у нас нельзя оскорблять или матерится в чате ${map[random]}  \uD83D\uDE19"
                                    )
                                } catch (e: Exception) {
                                    logger.error(e.message)
                                }
                            }
                        }
                    }
                }
            }

                  if (!isMadWord && sberEnable) {
                      runBlocking {
                          senderMessageSberImpl.addMessage(msg.chat.id, msg.messageId, msg.text.toString())
                          val result = senderMessageSberImpl.sendMessage()
                          logger.info(result.toString())
                          result.forEach { (chatId, messagesMap) ->
                              messagesMap.forEach { (messageId, responseMessage) ->
                                  var random = (0..3).random()
                                  if (responseMessage == "true") {
                                      try {
                                          isMadWord = responseMessage.toBoolean()
                                          bot.sendMessage(
                                              chatId.toChatId(),
                                              replyToMessageId = messageId,
                                              text = "у нас нельзя оскорблять или матерится в чате ${map[random]}  \uD83D\uDE19"
                                          )
                                      } catch (e: Exception) {
                                          logger.error(e.message)
                                      }
                                  }
                              }
                          }
                      }
                  }

            if (msg.text != null && !isMadWord) {
                val words: List<String> = msg.text?.split(" ", ".", ",", "?", "!")!!.toList()

                Log.info(msg.toString())

                for (w in words) {

                    if (wordsRepository.findByWordSimilarity(w.lowercase()).stream().count() > 0) {
                        logger.info("$msg")
                        logger.info("${msg.from!!.first_name} ${msg.from!!.lastName}")
                        var random = (0..3).random()
                        try {
                            bot.sendMessage(
                                msg.chat.id.toChatId(),
                                replyToMessageId = msg.messageId,
                                text = "у нас нельзя матерится в чате ${map[random]}  \uD83D\uDE19"
                            )
                        } catch (e: Exception) {
                            logger.error(e.message)
                        }
                        if (wordsRepository.findByWordOutInt(w.lowercase()) < 1) {
                            wordsRepository.save(Words().apply { word = w.lowercase() })
                        }
                        isWord = false
                    }
                }
                isMadWord = false
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

            if (!msg.forwardFromChat?.title.isNullOrBlank()) {
                if (msg.forwardFromChat!!.title == "Двач" || msg.forwardFromChat!!.title == "Ньюсач/Двач") {
                    bot.deleteMessage(msg.chat.id.toChatId(), msg.messageId)
                }
            }
        }

        bot.start()
    }
}