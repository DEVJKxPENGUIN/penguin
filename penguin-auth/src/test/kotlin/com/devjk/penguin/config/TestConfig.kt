package com.devjk.penguin.config

import com.devjk.penguin.external.GoogleApiHelper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.mockito.Mockito.spy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.reactive.function.client.WebClient
import redis.embedded.RedisServer

@TestConfiguration
class TestConfig(
    @Value("\${redis-port}")
    private val redisPort: String,
) {
    private val redisServer: RedisServer = RedisServer(redisPort.toInt())

    @Bean
    @Primary
    fun httpSession() = MockHttpSession()

    @Bean
    fun googleApiHelper(
        webClient: WebClient,
        @Value("\${google-client-id}")
        clientId: String,
        @Value("\${google-client-secret}")
        clientSecret: String
    ): GoogleApiHelper {
        val real = GoogleApiHelper(webClient, clientId, clientSecret)
        return spy(real)
    }

    @PostConstruct
    fun postConstruct() {
        redisServer.start()
    }

    @PreDestroy
    fun preDestroy() {
        redisServer.stop()
    }
}