package com.devjk.penguin.utils

import com.devjk.penguin.domain.oidc.OidcProvider

class UrlUtils {

    companion object {

        fun loginUrl(): String {
            return "${serverHome()}/user/login"
        }

        fun startOidcProviderUrl(provider: String, rd: String?): String {
            return "${serverAuth()}/start?provider=$provider&rd=${rd ?: serverHome()}"
        }

        fun userRegisterUrl(provider: OidcProvider, state: String): String {
            return "${serverHome()}/user/register?provider=$provider&state=$state"
        }

        fun signupUrl(): String {
            return "${serverAuth()}/signup"
        }

        fun logoutUrl(): String {
            return "${serverAuth()}/logout"
        }

        fun redirectUrl(): String {
            return "${serverAuth()}/callback"
        }

        fun serverAuth(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082"
            } else {
                "https://auth.devjk.me"
            }
        }

        fun serverHome(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8081"
            } else {
                "https://devjk.me"
            }
        }

    }

}