package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import org.springframework.stereotype.Service

@Service
class WordsService(private var wordsRepository: WordsRepository) {

    init {
        val token = "5903504093:AAGEWOrT2M-E6KLkjKAIchwMu8hwNx9s-Yk"
        val bot = Bot.createPolling(token)
        bot.onMessage { msg ->
            if (wordsRepository.findByWordOutInt(msg.text.toString()) > 0) {

                bot.sendMessage(msg.chat.id.toChatId(), text = "у нас нельзя матерится в чате")
                return@onMessage
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