package com.devjk.penguin.framework.common

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisHandler(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun <T> get(key: String, hashKey: String, clazz: Class<T>): T? {
        val ops = redisTemplate.opsForHash<String, String>()
        ops.get(key, hashKey)?.let {
            return if (clazz == String::class.java) it as T else objectMapper.readValue(it, clazz)
        }
        return null
    }

    fun <T> put(key: String, hashKey: String, value: T) {
        val ops = redisTemplate.opsForHash<String, String>()
        val input = if (value is String) value else objectMapper.writeValueAsString(value)
        ops.put(key, hashKey, input)
    }

    fun delete(key: String, hashKey: String) {
        val ops = redisTemplate.opsForHash<String, String>()
        ops.delete(key, hashKey)
    }
}