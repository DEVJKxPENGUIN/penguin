package com.devjk.penguin.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mock.web.MockHttpSession
import redis.embedded.RedisServer

@TestConfiguration
class TestConfig(
    @Value("\${redis-port}")
    private val redisPort: Int,
) {
    private lateinit var redisServer: RedisServer

    @Bean
    @Primary
    fun httpSession() = MockHttpSession()

    @PostConstruct
    fun postConstruct() {
        redisServer = RedisServer(redisPort)
        redisServer.start()
    }

    @PreDestroy
    fun preDestroy() {
        redisServer.stop()
    }
}
