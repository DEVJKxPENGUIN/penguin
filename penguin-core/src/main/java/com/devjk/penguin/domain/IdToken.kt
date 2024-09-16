package com.devjk.penguin.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdToken(
    val aud: String = "",
    val exp: String = "",
    val iat: String = "",
    val iss: String = "",
    val sub: String = "",
    val email: String = ""
) {

    companion object {

        fun from(token: String): IdToken {
            val encodedPayload = token.split(".")[1]
            val payload = String(Base64.getUrlDecoder().decode(encodedPayload))
            return ObjectMapper().readValue(payload, IdToken::class.java)
        }
    }
}