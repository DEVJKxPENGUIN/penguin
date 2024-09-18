package com.devjk.penguin.framework.aop

import com.devjk.penguin.db.entity.User
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.utils.Profiles
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Aspect
@Component
class AuthenticationAdvice(
    private val request: HttpServletRequest,
    private val webClient: WebClient
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
                    val userInfo = getAuthorizationHeader()
                    var user: User? = null
                    if (!userInfo.isNullOrBlank()) {
                        user = User.fromJsonEncoded(userInfo)
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
                .toBodilessEntity()
                .block()

            return authCallResponse?.headers
                ?.getFirst("Authorization")
        } else null
    }

}