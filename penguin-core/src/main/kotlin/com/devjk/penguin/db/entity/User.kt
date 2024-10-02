package com.devjk.penguin.db.entity

import com.devjk.penguin.framework.common.BaseEntity
import com.devjk.penguin.utils.JsonHelper
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user")
@EntityListeners
class User(
    @Id
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "nickname")
    val nickName: String = "",

    @Column(name = "email")
    val email: String = "",

    @Column(name = "id_token")
    val idToken: String = "",

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity(), Serializable {

    companion object {
        const val SESSION_TIME = 60L * 60L
    }

    @JsonIgnore
    fun isNotExpired(): Boolean {
        return lastLoginAt.plusSeconds(SESSION_TIME).isAfter(LocalDateTime.now())
    }

    fun renewSession() {
        lastLoginAt = LocalDateTime.now()
    }
}