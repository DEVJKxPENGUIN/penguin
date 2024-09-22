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
        const val AUTH_REDIRECT = "redirect"
    }

    @GetMapping("/auth")
    fun auth(alwaysSuccess: Boolean = false): ResponseEntity<*> {
        val userInfo: String?
        try {
            userInfo = authService.getUserAuthorization()
            userInfo ?: throw BaseException(ErrorCode.UNAUTHORIZED, "접근권한이 없습니다. 로그인 해주세요.")
        } catch (e: Exception) {
            if (alwaysSuccess) {
                return ResponseEntity.ok().body(BaseResponse.success())
            }
            throw e
        }

        return ResponseEntity.ok()
            .header("Authorization", "Bearer $userInfo")
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
        val idToken = authService.getOpenId(code)
        val user = authService.getRegisteredUser(idToken)
        val userInfo = authService.login(user)
        val rd = authService.getRedirectSession()

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .header("Authorization", "Bearer $userInfo")
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