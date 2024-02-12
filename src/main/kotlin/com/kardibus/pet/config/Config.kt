package com.kardibus.pet.config

import com.elbekd.bot.Bot
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit


@Configuration
class Config(@Value("\${telegram.token}") var token:String) {

    @Bean
    fun bot(): Bot {
        return Bot.createPolling(token)
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .build()
    }

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient.create()
            .option<Int>(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofMillis(5000))
            .doOnConnected { conn: Connection ->
                conn.addHandlerLast(ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
            }
    }
}