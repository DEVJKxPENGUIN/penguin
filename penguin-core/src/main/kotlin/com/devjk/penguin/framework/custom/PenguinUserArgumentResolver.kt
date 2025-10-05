package com.devjk.penguin.framework.custom

import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.utils.JwtUtils
import com.devjk.penguin.utils.PhaseUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.function.Predicate

@Component
class PenguinUserArgumentResolver(
    private val request: HttpServletRequest,
    private val jwtUtils: JwtUtils,
    private val webClient: WebClient,
    private val httpServletResponse: HttpServletResponse
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(PenguinUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val annotation: PenguinUser = parameter.getParameterAnnotation(PenguinUser::class.java)!!
        val idToken = getAuthorizationHeader()
        var user = AuthUser.ofGuest()
        if (!idToken.isNullOrBlank()) {
            val claims = jwtUtils.getClaimsWithVerify(idToken)
            claims?.let {
                val id = it.subject.toLong()
                val email = it["email"] as String
                val role = it["role"] as String
                val nickname = it["nickname"] as String
                user = AuthUser(id, email, Role.valueOf(role), nickname)
            }
        }

        if (!user.hasRole(annotation.min)) {

            if(annotation.redirectLoginPage && user.isGuest()) {
                val response = webRequest.getNativeResponse(HttpServletResponse::class.java)
                val request = webRequest.getNativeRequest(HttpServletRequest::class.java)

                var loginUrl = "/user/login"
                request?.let {
                    val originUrl = request.requestURL.toString()
                    val queryString = request.queryString
                    val rd = if (queryString.isNullOrBlank()) {
                        URLEncoder.encode(originUrl, StandardCharsets.UTF_8)
                    } else {
                        URLEncoder.encode("$originUrl?$queryString", StandardCharsets.UTF_8)
                    }
                    loginUrl = "$loginUrl?rd=$rd"
                }

                response?.sendRedirect(loginUrl)
                mavContainer?.isRequestHandled = true
                return null
            }

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
        return if (PhaseUtils.isLocal()) {
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