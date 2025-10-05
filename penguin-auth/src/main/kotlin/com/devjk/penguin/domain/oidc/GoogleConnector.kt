package com.devjk.penguin.domain.oidc

import com.devjk.penguin.external.GoogleApiHelper
import com.devjk.penguin.utils.JsonUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class GoogleConnector(
    private val googleApiHelper: GoogleApiHelper
) : Connector {

    override fun getProviderLink(state: String): String {
        return googleApiHelper.getGoogleLoginUrl(state)
    }

    override fun getProviderUserInfo(code: String): ProviderUserInfo? {
        return googleApiHelper.verifyOAuthCode(code)?.let {
            val encodedPayload = it.idToken.split(".")[1]
            val payload = String(Base64.getUrlDecoder().decode(encodedPayload))
            val payloadMap = JsonUtils.fromJson(payload, Map::class.java)
            val sub = payloadMap["sub"] as String
            val email = payloadMap["email"] as String
            return ProviderUserInfo(sub, email)
        }
    }
}