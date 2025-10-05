package com.devjk.penguin.service

import com.devjk.penguin.controller.OAuth2AuthorizeRequest
import com.devjk.penguin.controller.TokenRequest
import com.devjk.penguin.controller.TokenResponse
import com.devjk.penguin.db.entity.OidcProject
import com.devjk.penguin.db.entity.UserOidcProvision
import com.devjk.penguin.db.repository.OidcProjectRepository
import com.devjk.penguin.db.repository.UserOidcProvisionRepository
import com.devjk.penguin.db.repository.UserRepository
import com.devjk.penguin.domain.OAuth2AuthorizeStatus
import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.domain.oidc.OidcProvisionStatus
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.utils.JwtUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder

@Service
class OAuth2Service(
    private val oidcProjectRepository: OidcProjectRepository,
    private val userOidcProvisionRepository: UserOidcProvisionRepository,
    private val userRepository: UserRepository,
    private val jwtUtils: JwtUtils
) {

    fun createOidcProvide(request: OAuth2AuthorizeRequest, user: AuthUser): String {
        val oidcProject = this.getMatchedOidcProject(request)

        val oidcProvision = userOidcProvisionRepository.findByUserIdAndProjectId(
            user.id,
            oidcProject.id
        ) ?: UserOidcProvision.create(user.id, oidcProject.id)

        oidcProvision.renewCode()
        userOidcProvisionRepository.save(oidcProvision)

        return oidcProvision.code!!
    }

    fun getMatchedOidcProject(request: OAuth2AuthorizeRequest): OidcProject {
        val oidc = oidcProjectRepository.findByClientId(request.clientId!!)
            ?: throw BaseException(ErrorCode.INVALID_REQUEST, "Unknown Client ID")

        if (oidc.isNotMatchedUser(request.redirectUri!!, request.scope!!)) {
            throw BaseException(ErrorCode.INVALID_REQUEST, "Redirect URI or Scope does not matched")
        }
        return oidc
    }

    fun alreadyProvided(user: AuthUser, oidc: OidcProject): Boolean {
        return userOidcProvisionRepository.findByUserIdAndProjectIdAndStatus(
            user.id,
            oidc.id,
            OidcProvisionStatus.ACTIVE
        )
            ?.let { true } ?: false
    }

    fun createRedirectUri(
        request: OAuth2AuthorizeRequest,
        status: OAuth2AuthorizeStatus,
        code: String = ""
    ): String {
        val rd = UriComponentsBuilder.fromUriString(request.redirectUri!!)
            .queryParam("status", status.code)
            .queryParam("message", status.message)

        if (OAuth2AuthorizeStatus.SUCCESS == status) {
            rd.queryParam("code", code)
                .queryParam("state", request.state)
        }

        return "redirect:${rd.toUriString()}"
    }

    @Transactional
    fun provideOpenIdConnect(
        request: TokenRequest
    ): TokenResponse {
        val userId = request.code!!.split("-")[0].toLong()
        val oidcProvision = this.validateTokenRequest(userId, request)
        oidcProvision.activate()

        val user = userRepository.findById(userId)
            .orElseThrow {
                throw BaseException(ErrorCode.USER_NOT_FOUND, "User not found")
            }

        val idToken = jwtUtils.createIdToken(
            id = user.id,
            email = user.email,
            nickname = user.nickName,
            clientId = request.clientId!!
        )

        return TokenResponse(
            idToken = idToken,
            accessToken = idToken
        )
    }

    private fun validateTokenRequest(userId: Long, request: TokenRequest): UserOidcProvision {
        val oidcProvision = userOidcProvisionRepository.findByUserIdAndCode(userId, request.code!!)
            ?: throw BaseException(ErrorCode.INVALID_REQUEST, "Invalid code")

        val oidcProject = oidcProjectRepository.findById(oidcProvision.projectId)
            .orElseThrow {
                throw BaseException(ErrorCode.OIDC_PROJECT_NOT_FOUND, "Invalid OIDC project")
            }

        oidcProject.checkMatchedClient(request.clientId!!, request.clientSecret!!)

        return oidcProvision
    }
}