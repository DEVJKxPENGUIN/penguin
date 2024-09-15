package com.penguin.penguincore.domain

import java.time.LocalDateTime

data class User(
    val id: Long,
    val nickName: String,
    val email: String,
    val lastLoginAt: LocalDateTime
) {

    fun isNotExpired(sessionTime: Long): Boolean {
        return lastLoginAt.plusSeconds(sessionTime).isBefore(LocalDateTime.now())
    }
}