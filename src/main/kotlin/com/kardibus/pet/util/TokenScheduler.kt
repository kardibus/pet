package com.kardibus.pet.util

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class TokenScheduler(private val senderMessageSberImpl: SenderMessageSberImpl) {

    @Scheduled(fixedDelay = 20 * 60 * 1000) // 20 минут
    fun refreshToken() {
        senderMessageSberImpl.updateToken()
    }
}