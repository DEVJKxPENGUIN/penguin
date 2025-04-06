package com.devjk.penguin.service

import com.devjk.penguin.controller.AuthController.Companion.AUTH_REDIRECT
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.controller.AuthController.Companion.OAUTH_STATE
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.db.repository.UserRepository
import com.devjk.penguin.domain.auth.IdToken
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.external.GoogleApiHelper
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.utils.JwtHelper
import com.devjk.penguin.utils.UrlUtils
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.SecureRandom

@Service
class AuthService(
    private val session: HttpSession,
    private val jwtHelper: JwtHelper,
    private val userRepository: UserRepository,
    private val googleApiHelper: GoogleApiHelper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun getUserAuthorization(role: Role = Role.NORMAL): User {
        session.getAttribute(AUTH_VALUE)?.let {
            val user = it as User
            if (!user.hasRole(role)) {
                throw BaseException(ErrorCode.NO_AUTHORIZED_ROLE, "접근권한이 없습니다 : ${role.name}")
            }
            if (user.isNotExpired()) {
                user.renewSession()
                session.setAttribute(AUTH_VALUE, user)
                return user
            }
        }
        throw BaseException(ErrorCode.UNAUTHORIZED, "접근권한이 없습니다. 로그인 해주세요.")
    }

    fun isAuthenticated(): Boolean {
        try {
            getUserAuthorization()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun setStateToken(): String {
        val state = BigInteger(130, SecureRandom()).toString(32)
        session.setAttribute(OAUTH_STATE, state)
        return state
    }

    fun setRedirectSession(rd: String?) {
        rd?.let {
            session.setAttribute(AUTH_REDIRECT, rd)
        }
    }

    fun getRedirectSession(): String {
        return (session.getAttribute(AUTH_REDIRECT) ?: UrlUtils.serverHome()) as String
    }

    fun getOidcProviderLink(state: String): String {
        return googleApiHelper.getGoogleLoginUrl(state)
    }

    fun verifyStateToken(state: String) {
        val sessionState =
            session.getAttribute(OAUTH_STATE) ?: throw BaseException(ErrorCode.INVALID_STATETOKEN)
        val storedState = sessionState as String

        if (storedState != state) {
            throw BaseException(ErrorCode.INVALID_STATETOKEN)
        }
    }

    fun getOpenId(code: String): IdToken {
        return googleApiHelper.verifyOAuthCode(code)?.let {
            IdToken.from(it.idToken)
        } ?: throw BaseException(ErrorCode.UNAUTHORIZED)
    }

    fun getRegisteredUser(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw BaseException(ErrorCode.UNREGISTERED_USER)
    }

    fun login(user: User): String {
        user.renewSession()

        val jwt = jwtHelper.create(user.email, user.role.name, user.nickName)
        user.idToken = jwt
        userRepository.save(user)
        session.setAttribute(AUTH_VALUE, user)

        return jwt
    }

    fun logout() {
        session.invalidate()
    }
}