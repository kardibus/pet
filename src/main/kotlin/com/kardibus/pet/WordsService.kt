package com.kardibus.pet

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.UpdateMessage
import com.kardibus.pet.model.Words
import com.kardibus.pet.util.Message
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

val Log = LoggerFactory.getLogger(WordsService::class.java)

@Service
class WordsService(
    private var wordsRepository: WordsRepository,
    private val bot: Bot,
    private var messages: Message
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var isWord: Boolean = true

    private var map = mapOf(0 to "мразь", 1 to "хуесос", 2 to "уебок", 3 to "пидор")

    init {
        GlobalScope.launch { start() }
    }

    fun start() {
        bot.onAnyUpdate { upd ->
            Log.info(upd.toString())

            if (!(upd as UpdateMessage).message.forwardFromChat?.title.isNullOrBlank()) {
                if ((upd as UpdateMessage).message.forwardFromChat!!.title == "Двач" || (upd as UpdateMessage).message.forwardFromChat!!.title == "Ньюсач/Двач" ) {
                    var m = upd.message
                    bot.deleteMessage(m.chat.id.toChatId(), m.messageId)
                }
            }
        }
        bot.onMessage { msg ->
            isWord = true
            if (msg.text != null) {
                val words: List<String> = msg.text?.split(" ", ".", ",", "?", "!")!!.toList()

                Log.info(msg.toString())

                //     var message = messages.sendMessage(msg.text!!)

//                Log.info(message.toString())
//
//                if (message!!.result.alternatives.first().message.text.lowercase() == "true") {
//                    var random = (0..3).random()
//                    bot.sendMessage(
//                        msg.chat.id.toChatId(),
//                        replyToMessageId = msg.messageId,
//                        text = "у нас нельзя оскорблять или матерится в чате ${map.get(random)}  \uD83D\uDE19"
//                    )
//                }

                for (w in words) {
                    //     if (wordsRepository.findByWordOutInt(word.lowercase()) > 0 && isWord && word.isNotEmpty()) {
                    if (wordsRepository.findByWordSimilarity(w.lowercase()).stream().count() > 0) {
                        logger.info("$msg")
                        logger.info("${msg.from!!.first_name} ${msg.from!!.lastName}")
                        var random = (0..3).random()
                        bot.sendMessage(
                            msg.chat.id.toChatId(),
                            replyToMessageId = msg.messageId,
                            text = "у нас нельзя матерится в чате ${map[random]}  \uD83D\uDE19"
                        )
                        if (wordsRepository.findByWordOutInt(w.lowercase()) < 1) {
                          GlobalScope.launch {wordsRepository.save(Words().apply { word = w.lowercase() })}
                        }
                        isWord = false
                    }
                }
            }

            messages.addMessage(msg.text.toString())
            messages.startSendingMessages()

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