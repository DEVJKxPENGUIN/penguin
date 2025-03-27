package com.devjk.penguin.framework.aop

import com.devjk.penguin.domain.auth.AuthUser
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.utils.JwtHelper
import com.devjk.penguin.utils.Profiles
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.function.Predicate

@Aspect
@Component
class AuthenticationAdvice(
    private val request: HttpServletRequest,
    private val webClient: WebClient,
    private val jwtHelper: JwtHelper
) {

    @Pointcut(
        """
          @annotation(org.springframework.web.bind.annotation.RequestMapping)  
          || @annotation(org.springframework.web.bind.annotation.GetMapping)
          || @annotation(org.springframework.web.bind.annotation.PostMapping)
          || @annotation(org.springframework.web.bind.annotation.PutMapping)
          || @annotation(org.springframework.web.bind.annotation.DeleteMapping)
        """
    )
    fun requestMapping() {
    }

    @Around("requestMapping()")
    fun setPenguinUser(joinPoint: ProceedingJoinPoint): Any? {

        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args
        method.parameterAnnotations.forEachIndexed { index, annotations ->
            annotations.forEach { annotation ->
                if (annotation is PenguinUser) {
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
                            ErrorCode.NO_AUTHORIZED_ROLE,
                            "접근권한이 없습니다 : ${user.role.name}"
                        )
                    }

                    args[index] = user
                }
            }
        }

        return joinPoint.proceed(args)
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