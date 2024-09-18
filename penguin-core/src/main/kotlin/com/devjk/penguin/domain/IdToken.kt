package com.devjk.penguin.domain

import com.devjk.penguin.utils.JsonHelper
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
            return JsonHelper.fromJson(payload, IdToken::class.java)
        }
    }
}