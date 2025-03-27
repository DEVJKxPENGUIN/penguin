package com.devjk.penguin.domain.auth

enum class Role(private val level: Int) {
    GUEST(0),
    NORMAL(10),
    SUPER(999);

    fun isHigherOrEqualThan(role: Role): Boolean {
        return this.level >= role.level
    }
}