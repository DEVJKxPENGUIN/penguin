package com.devjk.penguin.utils

import com.devjk.penguin.domain.oidc.OidcProvider
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class UrlUtils {

    companion object {

        fun errorUrl(message: String?): String {
            return "${serverHome()}/errors?message=${
                URLEncoder.encode(
                    message,
                    StandardCharsets.UTF_8
                )
            }"
        }

        fun loginUrl(rd: String? = null): String {
            return "${serverHome()}/user/login${rd?.let { "?rd=$it" } ?: ""}"
        }

        fun oauthConsentAgreeUrl(): String {
            return "${serverHome()}/oauth2/consent/agree"
        }

        fun oauthConsentDisagreeUrl(): String {
            return "${serverHome()}/oauth2/consent/disagree"
        }

        fun startOidcProviderUrl(provider: String, rd: String?): String {
            return "${serverAuth()}/start?provider=$provider&rd=${URLEncoder.encode(rd ?: serverHome(), StandardCharsets.UTF_8)}"
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

        fun projectStartUrl(): String {
            return "${serverHome()}/project/start"
        }

        fun projectCreateUrl(): String {
            return "${serverHome()}/project"
        }

        fun projectUrl(oidcId: Long): String {
            return "${serverHome()}/project/$oidcId"
        }

        fun serverAuth(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082"
            } else {
                "https://auth.penguintribe.net"
            }
        }

        fun serverHome(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8081"
            } else {
                "https://penguintribe.net"
            }
        }

    }

}