package com.devjk.penguin.controller

import com.devjk.penguin.service.AuthService
import com.devjk.penguin.framework.common.BaseResponse
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) {
    companion object {
        const val AUTH_VALUE = "devjk_auth"
    }

    @GetMapping("/auth")
    fun auth(request: HttpServletRequest): ResponseEntity<*> {
        val sessionKey = authService.getUserAuthorization(request)
        sessionKey ?: throw BaseException(ErrorCode.UNAUTHORIZED, "접근권한이 없습니다. 로그인 해주세요.")

        return ResponseEntity.ok()
            .header("Authorization", sessionKey)
            .body(BaseResponse.success())
    }

    @GetMapping("/start")
    fun start(request: HttpServletRequest): ResponseEntity<*> {


        // fixme
        return ResponseEntity.ok()
            .body(BaseResponse.success())
    }
}