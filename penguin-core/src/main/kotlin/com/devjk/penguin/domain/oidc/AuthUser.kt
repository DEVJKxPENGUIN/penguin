package com.devjk.penguin.domain.oidc

data class AuthUser(
    val id: Long,
    val email: String,
    val role: Role,
    val nickname: String
) {
    companion object {
        fun ofGuest(): AuthUser {
            return AuthUser(0L, "anonymous", Role.GUEST, "anonymous")
        }
    }

    fun isGuest(): Boolean {
        return this.role == Role.GUEST
    }

    fun authenticated(): Boolean {
        return hasRole(Role.NORMAL)
    }

    fun hasRole(role: Role): Boolean {
        return this.role.isHigherOrEqualThan(role)
    }
}