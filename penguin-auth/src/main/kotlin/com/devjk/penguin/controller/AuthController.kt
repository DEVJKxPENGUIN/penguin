package com.devjk.penguin.controller

import com.devjk.penguin.db.entity.User
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.common.BaseResponse
import com.devjk.penguin.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class AuthController(
    private val authService: AuthService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    companion object {
        const val AUTH_VALUE = "devjk_auth"
        const val OAUTH_STATE = "oauth_state"
        const val AUTH_REDIRECT = "redirect"
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
    fun start(rd: String?): ResponseEntity<*> {
        val state = authService.setStateToken()
        authService.setRedirectSession(rd)
        val googleLoginUrl = authService.makeGoogleLoginUrl(state)

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(googleLoginUrl))
            .body(BaseResponse.success())
    }

    @GetMapping("/callback")
    fun callback(state: String, code: String): ResponseEntity<*> {
        log.info("/callback called -- state: $state, code: $code")

        authService.verifyStateToken(state)
        val oidcToken = authService.getOpenId(code)
        val user = authService.getRegisteredUser(oidcToken.email)
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
}