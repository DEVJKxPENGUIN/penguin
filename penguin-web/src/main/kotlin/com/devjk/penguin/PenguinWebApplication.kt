package com.devjk.penguin;

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
class PenguinWebApplication

fun main(args: Array<String>) {
    runApplication<PenguinWebApplication>(*args)
}

@PostConstruct
fun timezone() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
}