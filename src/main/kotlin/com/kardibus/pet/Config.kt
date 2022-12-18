package com.kardibus.pet

import com.elbekd.bot.Bot
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {

    private val token = "5903504093:AAGEWOrT2M-E6KLkjKAIchwMu8hwNx9s-Yk"

    @Bean
    fun bot(): Bot {
        return Bot.createPolling(token)
    }
}