package com.devjk.penguin.service

import com.devjk.penguin.controller.AuthController.Companion.AUTH_REDIRECT
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.controller.AuthController.Companion.OAUTH_STATE
import com.devjk.penguin.controller.AuthController.Companion.OIDC_PROVIDER
import com.devjk.penguin.controller.AuthController.Companion.SIGNUP_PROVIDER
import com.devjk.penguin.controller.AuthController.Companion.SIGNUP_STATE
import com.devjk.penguin.controller.AuthController.Companion.SIGNUP_USERINFO
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.db.repository.UserRepository
import com.devjk.penguin.domain.oidc.ConnectorFactory
import com.devjk.penguin.domain.oidc.OidcProvider
import com.devjk.penguin.domain.oidc.ProviderUserInfo
import com.devjk.penguin.domain.oidc.Role
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
    private val connectorFactory: ConnectorFactory,
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

    fun setStateToken(provider: OidcProvider): String {
        val state = BigInteger(130, SecureRandom()).toString(32)
        session.setAttribute(OAUTH_STATE, state)
        session.setAttribute(OIDC_PROVIDER, provider)
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

    fun getOidcProviderLink(provider: OidcProvider, state: String): String {
        val connector = connectorFactory.get(provider)
        return connector.getProviderLink(state)
    }

    fun verifyStateToken(state: String): OidcProvider {
        val sessionState =
            session.getAttribute(OAUTH_STATE) ?: throw BaseException(ErrorCode.INVALID_STATETOKEN)
        val storedState = sessionState as String

        if (storedState != state) {
            throw BaseException(ErrorCode.INVALID_STATETOKEN)
        }

        return session.getAttribute(OIDC_PROVIDER) as OidcProvider?
            ?: throw BaseException(ErrorCode.INVALID_STATETOKEN)
    }

    fun getProviderUserInfo(provider: OidcProvider, code: String): ProviderUserInfo {
        val connector = connectorFactory.get(provider)
        return connector.getProviderUserInfo(code) ?: throw BaseException(ErrorCode.UNAUTHORIZED)
    }

    fun getRegisteredUser(oidcProvider: OidcProvider, providerUserInfo: ProviderUserInfo): User? {
        return userRepository.findByProviderAndProviderId(oidcProvider, providerUserInfo.id)
    }

    fun prepareSignup(oidcProvider: OidcProvider, providerUserInfo: ProviderUserInfo): String {
        val state = BigInteger(130, SecureRandom()).toString(32)
        session.setAttribute(SIGNUP_PROVIDER, oidcProvider)
        session.setAttribute(SIGNUP_USERINFO, providerUserInfo)
        session.setAttribute(SIGNUP_STATE, state)
        return state
    }

    fun signup(state: String, nickName: String): User {
        val sessionState = session.getAttribute(SIGNUP_STATE) as String?
            ?: throw BaseException(ErrorCode.INVALID_SIGNUP_ACCESS, "잘못된 접근입니다.")

        val providerUserInfo = session.getAttribute(SIGNUP_USERINFO) as ProviderUserInfo?
            ?: throw BaseException(ErrorCode.INVALID_SIGNUP_ACCESS, "잘못된 접근입니다.")

        val oidcProvider = session.getAttribute(SIGNUP_PROVIDER) as OidcProvider?
            ?: throw BaseException(ErrorCode.INVALID_SIGNUP_ACCESS, "잘못된 접근입니다.")

        if (sessionState != state) {
            throw BaseException(ErrorCode.INVALID_SIGNUP_ACCESS, "잘못된 접근입니다.")
        }

        val user = userRepository.save(
            User(
                provider = oidcProvider,
                providerId = providerUserInfo.id,
                nickName = nickName,
                email = providerUserInfo.email,
                role = Role.NORMAL
            )
        )

        session.removeAttribute(SIGNUP_PROVIDER)
        session.removeAttribute(SIGNUP_USERINFO)
        session.removeAttribute(SIGNUP_STATE)
        return user
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