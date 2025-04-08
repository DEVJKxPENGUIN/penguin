package com.devjk.penguin.external

import com.devjk.penguin.utils.UrlUtils
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class GithubApiHelper(
    private val webClient: WebClient,
    @Value("\${github-client-id}")
    private val clientId: String,
    @Value("\${github-client-secret}")
    private val clientSecret: String
) {

    private val GITHUB_URL = "https://github.com"
    private val GITHUB_API_URL = "https://api.github.com"

    fun getGithubLoginUrl(state: String): String {
        val encodedRedirect = URLEncoder.encode(UrlUtils.redirectUrl(), StandardCharsets.UTF_8)
        val encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8)
        val encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8)

        return "$GITHUB_URL/login/oauth/authorize?" +
                "client_id=$encodedClientId" +
                "&scope=user" +
                "&redirect_uri=$encodedRedirect" +
                "&state=$encodedState"
    }

    fun getAccessToken(code: String): GithubAccessToken? {
        return webClient
            .post()
            .uri("$GITHUB_URL/login/oauth/access_token")
            .body(
                BodyInserters.fromFormData("code", code)
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("redirect_uri", UrlUtils.redirectUrl())
            )
            .retrieve()
            .bodyToMono(GithubAccessToken::class.java)
            .block()
    }

    fun getUser(accessToken: String): GithubUser? {
        return webClient
            .get()
            .uri("$GITHUB_API_URL/user")
            .headers {
                it.setBearerAuth(accessToken)
            }
            .retrieve()
            .bodyToMono(GithubUser::class.java)
            .block()
    }

}

data class GithubAccessToken(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("scope")
    val scope: String
)

// 스키마 추가 가능
// https://docs.github.com/ko/rest/users/users?apiVersion=2022-11-28#get-the-authenticated-user
data class GithubUser(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("email")
    val email: String?
)