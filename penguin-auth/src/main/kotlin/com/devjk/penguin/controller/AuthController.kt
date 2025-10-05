package com.devjk.penguin.controller

import com.devjk.penguin.db.entity.User
import com.devjk.penguin.domain.oidc.OidcProvider
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.framework.common.BaseResponse
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.service.AuthService
import com.devjk.penguin.utils.HostUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class AuthController(
    private val authService: AuthService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    companion object {
        const val AUTH_VALUE = "devjk_auth"
        const val OIDC_PROVIDER = "oidc_provider"
        const val OAUTH_STATE = "oauth_state"
        const val AUTH_REDIRECT = "redirect"
        const val SIGNUP_PROVIDER = "signup_provider"
        const val SIGNUP_USERINFO = "signup_idtoken"
        const val SIGNUP_STATE = "signup_state"
    }

    /**
     * nginx ingress auth 에서 항상 검증한다.
     */
    @GetMapping("/auth")
    fun auth(alwaysSuccess: Boolean = false, role: Role = Role.NORMAL): ResponseEntity<*> {
        val user: User
        try {
            user = authService.getUserAuthorization(role)
        } catch (e: Exception) {
            if (alwaysSuccess) {
                return ResponseEntity.ok()
                    .header("Authorization", "Bearer ")
                    .body(BaseResponse.success())
            }
            throw e
        }

        return ResponseEntity.ok()
            .header("Authorization", "Bearer ${user.idToken}")
            .body(BaseResponse.success())
    }

    @GetMapping("/start")
    fun startProvider(provider: String = "google", rd: String?): ResponseEntity<*> {
        val oidcProvider: OidcProvider
        try {
            oidcProvider = OidcProvider.valueOf(provider)
        } catch (e: Exception) {
            throw BaseException(ErrorCode.INVALID_OIDC_PROVIDER, "지원하지 않는 OpenId Provider 입니다.")
        }

        val state = authService.setStateToken(oidcProvider)
        authService.setRedirectSession(rd)
        val oidcLoginUrl = authService.getOidcProviderLink(oidcProvider, state)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(oidcLoginUrl))
            .body(BaseResponse.success())
    }

    @GetMapping("/callback")
    fun callback(state: String, code: String): ResponseEntity<*> {
        log.info("/callback called -- state: $state, code: $code")

        val oidcProvider = authService.verifyStateToken(state)
        val providerUserInfo = authService.getProviderUserInfo(oidcProvider, code)
        val user = authService.getRegisteredUser(oidcProvider, providerUserInfo)
            ?: run {
                val signupState = authService.prepareSignup(oidcProvider, providerUserInfo)
                return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(
                        "Location",
                        HostUtils.userRegisterUrl(oidcProvider, signupState)
                    )
                    .body(BaseResponse.success())
            }

        return loginWithRedirect(user)
    }

    @PostMapping("/signup")
    fun signup(state: String, nickname: String): ResponseEntity<*> {
        val user = authService.signup(state, nickname)
        return loginWithRedirect(user)
    }

    @GetMapping("/logout")
    fun logout(rd: String?): ResponseEntity<*> {
        authService.logout()
        if (rd.isNullOrBlank()) {
            return ResponseEntity.ok().body(BaseResponse.success())
        }
        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", rd)
            .body(BaseResponse.success())
    }

    private fun loginWithRedirect(user: User): ResponseEntity<*> {
        val idToken = authService.login(user)
        val rd = authService.getRedirectSession()

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .header("Authorization", "Bearer $idToken")
            .header("Location", rd)
            .body(
                BaseResponse.success(
                    mapOf(
                        "email" to user.email,
                    )
                )
            )
    }
}