package com.devjk.penguin.domain

import com.devjk.penguin.utils.JsonHelper
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdToken(
    var aud: String = "",
    var exp: String = "",
    var iat: String = "",
    var iss: String = "",
    var sub: String = "",
    var email: String = "",
    var origin: String = ""
) {

    companion object {

        fun from(token: String): IdToken {
            return IdToken(origin = token)
        }
    }

    init {
        val encodedPayload = origin.split(".")[1]
        val payload = String(Base64.getUrlDecoder().decode(encodedPayload))
        val payloadMap = JsonHelper.fromJson(payload, Map::class.java)
        this.aud = payloadMap["aud"] as String
        this.exp = payloadMap["exp"] as String
        this.iat = payloadMap["iat"] as String
        this.iss = payloadMap["iss"] as String
        this.sub = payloadMap["sub"] as String
        this.email = payloadMap["email"] as String
    }
}