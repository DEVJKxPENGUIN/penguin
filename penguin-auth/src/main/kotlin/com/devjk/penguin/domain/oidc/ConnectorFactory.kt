package com.devjk.penguin.domain.oidc

import org.springframework.stereotype.Component

@Component
class ConnectorFactory(
    private val googleConnector: GoogleConnector,
    private val githubConnector: GithubConnector,
) {

    fun get(oidcProvider: OidcProvider): Connector {
        return when (oidcProvider) {
            OidcProvider.google -> googleConnector
            OidcProvider.github -> githubConnector
        }
    }
}