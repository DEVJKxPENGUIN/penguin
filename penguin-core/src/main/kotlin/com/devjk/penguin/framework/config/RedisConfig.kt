package com.devjk.penguin.framework.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession
import java.time.Duration

@Configuration
@EnableRedisIndexedHttpSession
class RedisConfig(
    @Value("\${redis-host}")
    private val redisHost: String,
    @Value("\${redis-port}")
    private val redisPort: String,
    @Value("\${redis-password}")
    private val redisPassword: String,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(2))
            .shutdownTimeout(Duration.ZERO)
            .build()

        val connectConfig = RedisStandaloneConfiguration(redisHost, redisPort.toInt())
        connectConfig.password = RedisPassword.of(redisPassword)

        return LettuceConnectionFactory(connectConfig, clientConfig)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }
}