package com.devjk.penguin.domain.auth

data class AuthUser(
    val email: String,
    val role: Role,
    val nickname: String
) {
    companion object {
        fun ofGuest(): AuthUser {
            return AuthUser("GUEST_USER", Role.GUEST, "GUEST_USER")
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