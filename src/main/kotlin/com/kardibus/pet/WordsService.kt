package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random


@Service
class WordsService(private var wordsRepository: WordsRepository, private val bot: Bot) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var isWord: Boolean = true

    private var map = mapOf(0 to "мразь",1 to "хуесос", 2 to "уебок", 3 to "пидор")

    init {
        GlobalScope.launch { start() }
    }

    fun start() {
        bot.onMessage { msg ->
            isWord = true
            if (msg.text != null) {
                val words: List<String> = msg.text?.split(" ", ".", ",", "?", "!")!!.toList()

                for (word in words) {
                    if (wordsRepository.findByWordOutInt(word.lowercase()) > 0 && isWord && word.isNotEmpty()) {

                        logger.info("$msg")
                        logger.info("${msg.from!!.first_name} ${msg.from!!.lastName}")
                        var random = (0..3).random()
                        bot.sendMessage(
                            msg.chat.id.toChatId(),
                            replyToMessageId = msg.messageId,
                            text = "у нас нельзя матерится в чате ${map.get(random)}  \uD83D\uDE19"
                        )
                        isWord = false
                    }
                }
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