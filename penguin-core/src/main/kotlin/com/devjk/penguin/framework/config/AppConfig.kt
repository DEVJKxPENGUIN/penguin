package com.devjk.penguin.framework.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
class AppConfig {

    @Bean
    fun objectMapper() = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

}
