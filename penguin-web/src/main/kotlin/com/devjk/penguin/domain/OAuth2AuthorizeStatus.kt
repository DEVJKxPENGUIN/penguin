package com.devjk.penguin.domain

enum class OAuth2AuthorizeStatus(
    val code: Int,
    val message: String
) {
    SUCCESS(0, "ok"),
    ALREADY_PROVIDED(-1, "already provided"),
    USER_DENIED(-2, "user denied")
    ;
}