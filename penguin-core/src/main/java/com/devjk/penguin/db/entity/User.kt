package com.devjk.penguin.db.entity

import com.devjk.penguin.framework.common.BaseEntity
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "user")
@EntityListeners
class User(
    @Id
    @Column(name = "id")
    val id: Long,

    @Column(name = "nickname")
    val nickName: String,

    @Column(name = "email")
    val email: String,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime
) : BaseEntity(), Serializable {

    companion object {
        const val SESSION_TIME = 60L * 60L
    }

    fun isNotExpired(): Boolean {
        return lastLoginAt.plusSeconds(SESSION_TIME).isAfter(LocalDateTime.now())
    }

    fun renewSession() {
        lastLoginAt = LocalDateTime.now()
    }
}