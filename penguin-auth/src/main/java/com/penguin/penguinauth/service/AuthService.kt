package com.penguin.penguinauth.service

import com.penguin.penguinauth.controller.AuthController.Companion.AUTH_COOKIE
import com.penguin.penguinauth.controller.AuthController.Companion.SESSION_TIME
import com.penguin.penguinauth.framework.common.RedisHandler
import com.penguin.penguincore.domain.User
import com.penguin.penguincore.framework.error.ErrorCode
import com.penguin.penguincore.framework.error.exception.BaseException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val redisHandler: RedisHandler
) {


    fun getUserAuthorization(request: HttpServletRequest): String {
        val cookie = getAuthCookie(request)
        cookie?.let { ck ->
            redisHandler.get("authorization", ck.value, User::class.java)?.let { user ->
                if (user.isNotExpired(SESSION_TIME)) {
                    return ck.value
                }
            }
        }
        throw BaseException(ErrorCode.UNAUTHORIZED, "접근권한이 없습니다. 로그인 해주세요.")
    }

    private fun getAuthCookie(request: HttpServletRequest): Cookie? {
        return request.cookies.firstOrNull { it.name == AUTH_COOKIE }
    }

}