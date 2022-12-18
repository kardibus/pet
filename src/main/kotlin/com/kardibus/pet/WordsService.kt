package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import org.springframework.stereotype.Service


@Service
class WordsService(private var wordsRepository: WordsRepository) {

    private var isWord: Boolean = true

    init {
        val token = "5903504093:AAGEWOrT2M-E6KLkjKAIchwMu8hwNx9s-Yk"
        val bot = Bot.createPolling(token)
        bot.onMessage { msg ->
            isWord = true
            if (msg.text != null) {
                val words: List<String> = msg.text?.split(" ")!!.toList()

                for (word in words) {
                    if (wordsRepository.findByWordOutInt(word.lowercase()) > 0 && isWord) {

                        bot.sendMessage(
                            msg.chat.id.toChatId(),
                            replyToMessageId = msg.messageId,
                            text = "у нас нельзя матерится в чате  \uD83D\uDE19"
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