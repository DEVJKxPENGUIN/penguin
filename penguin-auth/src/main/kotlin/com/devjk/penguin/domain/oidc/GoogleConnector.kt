package com.devjk.penguin.domain.oidc

import com.devjk.penguin.external.GoogleApiHelper
import org.springframework.stereotype.Component

@Component
class GoogleConnector(
    private val googleApiHelper: GoogleApiHelper
) : Connector {

    override fun getProviderLink(state: String): String {
        return googleApiHelper.getGoogleLoginUrl(state)
    }

    override fun getOpenId(code: String): IdToken? {
        return googleApiHelper.verifyOAuthCode(code)?.let {
            IdToken.from(it.idToken)
        }
    }
}