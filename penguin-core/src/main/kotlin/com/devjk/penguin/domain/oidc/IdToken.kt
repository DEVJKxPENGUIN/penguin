package com.devjk.penguin.domain.oidc

import com.devjk.penguin.utils.JsonHelper
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdToken(
    var aud: String = "",
    var exp: String = "",
    var iat: String = "",
    var iss: String = "",
    var sub: String = "",
    var email: String = "",
    var role: String = "",
    var origin: String = ""
) : Serializable {

    companion object {
        fun from(token: String, role: Role = Role.GUEST): IdToken {
            return IdToken(origin = token, role = role.name)
        }
    }

    init {
        val encodedPayload = origin.split(".")[1]
        val payload = String(Base64.getUrlDecoder().decode(encodedPayload))
        val payloadMap = JsonHelper.fromJson(payload, Map::class.java)
        this.aud = payloadMap["aud"] as String
        this.exp = (payloadMap["exp"] as Int).toString()
        this.iat = (payloadMap["iat"] as Int).toString()
        this.iss = payloadMap["iss"] as String
        this.sub = payloadMap["sub"] as String
        this.email = payloadMap["email"] as String
    }
}