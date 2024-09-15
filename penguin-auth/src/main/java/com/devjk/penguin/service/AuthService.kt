package com.devjk.penguin.service

import com.devjk.penguin.framework.common.RedisHandler
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.SecureRandom

@Service
class AuthService(
    private val redisHandler: RedisHandler
) {
    private val log = LoggerFactory.getLogger(this.javaClass)


    fun getUserAuthorization(request: HttpServletRequest): String? {

        val session = request.getSession()

        log.info("session --- ${request.session}")

        return "test"
    }

    fun setStateToken(request: HttpServletRequest) {
        val state = BigInteger(130, SecureRandom()).toString(32)
        request.session.setAttribute("state", state)
    }


}