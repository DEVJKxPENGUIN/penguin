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

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity(), Serializable {

    companion object {
        const val SESSION_TIME = 60L * 60L

        fun fromJsonEncoded(str: String): User {
            val userJson = String(Base64.getUrlDecoder().decode(str))
            return JsonHelper.fromJson(userJson, User::class.java)
        }
    }

    @JsonIgnore
    fun isNotExpired(): Boolean {
        return lastLoginAt.plusSeconds(SESSION_TIME).isAfter(LocalDateTime.now())
    }

    fun renewSession() {
        lastLoginAt = LocalDateTime.now()
    }

    fun toJsonEncoded(): String {
        val json = JsonHelper.toJson(this)
        val encoded = Base64.getUrlEncoder().encode(json.toByteArray())
        return String(encoded)
    }
}