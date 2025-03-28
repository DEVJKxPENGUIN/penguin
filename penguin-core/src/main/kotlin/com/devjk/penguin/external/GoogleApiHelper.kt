package com.devjk.penguin.external

import com.devjk.penguin.domain.auth.GoogleOpenId
import com.devjk.penguin.utils.UrlUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class GoogleApiHelper(
    private val webClient: WebClient,
    @Value("\${google-client-id}")
    private val clientId: String,
    @Value("\${google-client-secret}")
    private val clientSecret: String,
) {

    private val GOOGLE_ACCOUNT_URL = "https://accounts.google.com"
    private val GOOGLE_OAUTH_URL = "https://oauth2.googleapis.com"

    fun getGoogleLoginUrl(state: String): String {
        val encodedRedirect = URLEncoder.encode(UrlUtils.redirectUrl(), StandardCharsets.UTF_8)
        val encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8)
        val encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8)

        return "$GOOGLE_ACCOUNT_URL/o/oauth2/v2/auth?" +
                "response_type=code" +
                "&client_id=$encodedClientId" +
                "&scope=openid%20email" +
                "&redirect_uri=$encodedRedirect" +
                "&state=$encodedState"
    }

    fun verifyOAuthCode(code: String): GoogleOpenId? {
        return webClient
            .post()
            .uri("$GOOGLE_OAUTH_URL/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("code", code)
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("redirect_uri", UrlUtils.redirectUrl())
                    .with("grant_type", "authorization_code")
            )
            .retrieve()
            .bodyToMono(GoogleOpenId::class.java)
            .block()
    }


}