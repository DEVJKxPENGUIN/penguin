package com.devjk.penguin.domain.oidc

import org.springframework.stereotype.Component

@Component
class GithubConnector : Connector {

    override fun getProviderLink(state: String): String {
        TODO("Not yet implemented")
    }

    override fun getOpenId(code: String): IdToken {
        TODO("Not yet implemented")
    }
}