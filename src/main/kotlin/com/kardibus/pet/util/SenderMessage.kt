package com.kardibus.pet.util

interface SenderMessage {
    fun sendMessage(): MutableMap<Long, MutableMap<Long, String>>
}