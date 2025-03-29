package com.devjk.penguin.framework.custom

import com.devjk.penguin.domain.auth.AuthUser
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.utils.JwtHelper
import com.devjk.penguin.utils.Profiles
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.function.Predicate

@Component
class PenguinUserArgumentResolver(
    private val request: HttpServletRequest,
    private val jwtHelper: JwtHelper,
    private val webClient: WebClient
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(PenguinUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val annotation: PenguinUser = parameter.getParameterAnnotation(PenguinUser::class.java)!!
        val idToken = getAuthorizationHeader()
        var user = AuthUser.ofGuest()
        if (!idToken.isNullOrBlank()) {
            val claims = jwtHelper.getClaimsWithVerify(idToken)
            claims?.let {
                val email = it.subject
                val role = it["role"] as String
                val nickname = it["nickname"] as String
                user = AuthUser(email, Role.valueOf(role), nickname)
            }
        }

        if (!user.hasRole(annotation.min)) {
            throw BaseException(
                ErrorCode.NO_AUTHORIZED_ROLE, "접근권한이 없습니다 : ${user.role.name}"
            )
        }

        return user
    }

    private fun getAuthorizationHeader(): String? {
        val authHeader = request.getHeader("Authorization") ?: getFromPenguinAuth()
        return authHeader?.replace("Bearer ", "")
    }

    private fun getFromPenguinAuth(): String? {
        val sessionCookie = request.cookies?.find { it.name == "devjksession" }
        return if (Profiles.isLocal()) {
            val authCallResponse = webClient.get()
                .uri("http://localhost:8082/auth")
                .cookie("devjksession", sessionCookie?.value ?: "")
                .retrieve()
                .onStatus(Predicate.isEqual(HttpStatus.UNAUTHORIZED)) {
                    Mono.empty()
                }
                .toBodilessEntity()
                .block()

            return authCallResponse?.headers
                ?.getFirst("Authorization")
        } else null
    }
}