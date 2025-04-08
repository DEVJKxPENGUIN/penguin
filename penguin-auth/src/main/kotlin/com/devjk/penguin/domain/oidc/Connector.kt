package com.devjk.penguin.domain.oidc

interface Connector {
    fun getProviderLink(state: String): String
    fun getProviderUserInfo(code: String): ProviderUserInfo?
}