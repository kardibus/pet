package com.kardibus.pet.util

interface SenderMessage<T,H> {
    fun sendMessage(): MutableMap<T, MutableMap<T, H>>
}