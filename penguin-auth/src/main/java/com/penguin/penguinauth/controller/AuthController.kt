package com.penguin.penguinauth.controller

import com.penguin.penguinauth.service.AuthService
import com.penguin.penguincore.framework.common.BaseResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) {
    companion object {
        const val AUTH_COOKIE = "devjk_session"
        const val SESSION_TIME = 60 * 60L
    }

    @GetMapping("/auth")
    fun auth(request: HttpServletRequest): ResponseEntity<*> {
        val sessionKey = authService.getUserAuthorization(request)

        return ResponseEntity.ok()
            .header("Authorization", sessionKey)
            .body(BaseResponse.success())
    }
}