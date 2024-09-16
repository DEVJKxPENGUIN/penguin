package com.devjk.penguin.controller

import com.devjk.penguin.framework.common.BaseResponse
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
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
    }

    @GetMapping("/auth")
    fun auth(): ResponseEntity<*> {
        val sessionKey = authService.getUserAuthorization()
        sessionKey ?: throw BaseException(ErrorCode.UNAUTHORIZED, "접근권한이 없습니다. 로그인 해주세요.")

        return ResponseEntity.ok()
            .header("Authorization", sessionKey)
            .body(BaseResponse.success())
    }

    @GetMapping("/start")
    fun start(): ResponseEntity<*> {
        val state = authService.setStateToken()
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
        val idToken = authService.getOpenId(code)
        val user = authService.getRegisteredUser(idToken)
        val sessionKey = authService.login(user)

        return ResponseEntity.ok()
            .header("Authorization", sessionKey)
            .body(
                BaseResponse.success(
                    mapOf(
                        "email" to user.email,
                    )
                )
            )
    }
}