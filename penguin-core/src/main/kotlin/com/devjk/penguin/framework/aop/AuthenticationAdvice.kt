package com.devjk.penguin.framework.aop

import com.devjk.penguin.db.entity.User
import com.devjk.penguin.framework.annotation.PenguinUser
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Aspect
@Component
class AuthenticationAdvice(
    private val request: HttpServletRequest,
    private val redisTemplate: RedisTemplate<String, String>
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
                    val userInfo = request.getHeader("Authorization")?.replace("Bearer ", "")
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

}