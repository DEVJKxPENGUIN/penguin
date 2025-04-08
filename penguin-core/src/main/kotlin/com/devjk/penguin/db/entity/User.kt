package com.devjk.penguin.db.entity

import com.devjk.penguin.domain.oidc.OidcProvider
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.framework.common.BaseEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "\"user\"")
@EntityListeners
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    val provider: OidcProvider,

    @Column(name = "provider_id")
    val providerId: String,

    @Column(name = "nickname")
    val nickName: String = "",

    @Column(name = "email")
    val email: String = "",

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var role: Role = Role.NORMAL,

    @Column(name = "id_token", columnDefinition = "text")
    var idToken: String = "",

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

    fun hasRole(role: Role): Boolean {
        return this.role.isHigherOrEqualThan(role)
    }
}