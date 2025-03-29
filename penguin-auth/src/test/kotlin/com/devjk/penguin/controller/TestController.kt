package com.devjk.penguin.controller

import com.devjk.penguin.domain.auth.AuthUser
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.framework.common.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController(
    private val mapper: ObjectMapper
) {

    @GetMapping("/auth/advice/guest")
    fun authAdviceAll(
        @PenguinUser(min = Role.GUEST) user: AuthUser
    ): ResponseEntity<*> {
        return ResponseEntity.ok()
            .body(BaseResponse.success(user))
    }

    @GetMapping("/auth/advice/normal")
    fun authAdviceNormal(
        @PenguinUser(min = Role.NORMAL) user: AuthUser
    ): ResponseEntity<*> {
        return ResponseEntity.ok()
            .body(BaseResponse.success(user))
    }

    @GetMapping("/auth/advice/super")
    fun authAdviceAdmin(
        @PenguinUser(min = Role.SUPER) user: AuthUser
    ): ResponseEntity<*> {
        return ResponseEntity.ok()
            .body(BaseResponse.success(user))
    }

}