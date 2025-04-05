package com.devjk.penguin.domain.auth

enum class Role(
    private val level: Int,
    private val description: String
) {
    GUEST(0, "익명"),
    NORMAL(10, "유저"),
    SUPER(999, "관리자");

    fun isHigherOrEqualThan(role: Role): Boolean {
        return this.level >= role.level
    }

    fun description(): String {
        return this.description
    }
}