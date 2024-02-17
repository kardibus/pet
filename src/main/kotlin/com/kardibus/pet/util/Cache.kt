package com.kardibus.pet.util

abstract class Cache<T,H>:SenderMessage<T,H> {

    var delayGlobal = 1000
    var cache: MutableMap<T, MutableMap<T, H>> = HashMap()

    fun addMessage(chatId: T, messageId: T, message: H) {
        cache.putIfAbsent(chatId, mutableMapOf())
        cache[chatId]?.put(messageId, message)
    }
}