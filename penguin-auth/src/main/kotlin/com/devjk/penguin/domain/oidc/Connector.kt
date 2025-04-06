package com.devjk.penguin.domain.oidc

interface Connector {
    fun getProviderLink(state: String): String
    fun getOpenId(code: String): IdToken?
}