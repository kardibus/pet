package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WordsService( @Value("token") private  var token:String,private var wordsRepository: WordsRepository) {

    init {


        val bot = Bot.createPolling(token)

        bot.onMessage { msg ->
            if (wordsRepository.findByWordOutInt(msg.text.toString())>0) {

                bot.sendMessage(msg.chat.id.toChatId(), text = "у нас нельзя матерится в чате")
                return@onMessage
            }

            if (!msg.newChatMembers.isNullOrEmpty()) {
                bot.sendMessage(
                    msg.chat.id.toChatId(),
                    "Приветствуем тебя в чате ${msg.newChatMembers.first().first_name} ${msg.newChatMembers.first().lastName}"
                )
            }

            if (msg.leftChatMember?.id!=null) {
                bot.sendMessage(
                    msg.chat.id.toChatId(),
                    "Прощай ${msg.leftChatMember?.first_name} ${msg.leftChatMember?.lastName}"
                )
            }
        }

//        bot.chain(
//            label = "new_chat_members",
//            predicate = { msg -> !msg.newChatMembers.isNullOrEmpty() },
//            action = { msg ->
//                if (!msg.newChatMembers.isNullOrEmpty()) {
//                    bot.sendMessage(
//                        msg.chat.id.toChatId(),
//                        "Приветствуем тебя в чате ${msg.newChatMembers.first().first_name} ${msg.newChatMembers.first().lastName}"
//                    )
//                }
//            }).build()

        bot.start()
    }
}