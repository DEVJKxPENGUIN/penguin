package com.devjk.penguin.service

import com.devjk.penguin.controller.AuthController.Companion.AUTH_REDIRECT
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.controller.AuthController.Companion.OAUTH_STATE
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.db.repository.UserRepository
import com.devjk.penguin.domain.GoogleOpenId
import com.devjk.penguin.domain.IdToken
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.utils.UrlUtils
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

@Service
class AuthService(
    private val session: HttpSession,
    private val webClient: WebClient,
    @Value("\${google-client-id}")
    private val clientId: String,
    @Value("\${google-client-secret}")
    private val clientSecret: String,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun getUserAuthorization(): String? {
        session.getAttribute(AUTH_VALUE)?.let {
            val user = it as User
            if (user.isNotExpired()) {
                user.renewSession()
                session.setAttribute(AUTH_VALUE, user)
                return session.id
            }
        }
        return null
    }

    fun setStateToken(): String {
        val state = BigInteger(130, SecureRandom()).toString(32)
        session.setAttribute(OAUTH_STATE, state)
        return state
    }

    fun setRedirectSession(rd: String?) {
        rd?.let {
            session.setAttribute(AUTH_REDIRECT, rd)
        }
    }

    fun getRedirectSession(): String {
        return (session.getAttribute(AUTH_REDIRECT) ?: UrlUtils.serverHome()) as String
    }

    fun makeGoogleLoginUrl(state: String): String {
        val sb = StringBuilder("https://accounts.google.com/o/oauth2/v2/auth?")
        sb.append("response_type=code")
        sb.append("&client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}")
        sb.append("&scope=openid%20email")
        sb.append(
            "&redirect_uri=${
                URLEncoder.encode(
                    UrlUtils.redirectUrl(),
                    StandardCharsets.UTF_8
                )
            }"
        )
        sb.append("&state=${URLEncoder.encode(state, StandardCharsets.UTF_8)}")
        sb.append(
            "&login_hint=${
                URLEncoder.encode(
                    "dfjung4254@gmail.com",
                    StandardCharsets.UTF_8
                )
            }"
        )
        return sb.toString()
    }

    fun verifyStateToken(state: String) {
        var sessionState =
            session.getAttribute(OAUTH_STATE) ?: throw BaseException(ErrorCode.INVALID_STATETOKEN)
        val storedState = sessionState as String

        if (storedState != state) {
            throw BaseException(ErrorCode.INVALID_STATETOKEN)
        }
    }

    fun getOpenId(code: String): IdToken {
        val googleOpenId = webClient
            .post()
            .uri("https://oauth2.googleapis.com/token")
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

        return googleOpenId?.let { IdToken.from(it.idToken) }
            ?: throw BaseException(ErrorCode.UNAUTHORIZED)
    }

    fun getRegisteredUser(idToken: IdToken): User {
        return userRepository.findByEmail(idToken.email)
            ?: throw BaseException(ErrorCode.UNAUTHORIZED)
    }

    fun login(user: User): String {
        user.renewSession()
        userRepository.save(user)
        session.setAttribute(AUTH_VALUE, user)
        return session.id
    }

    fun logout() {
        session.invalidate()
    }
}