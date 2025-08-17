package com.devjk.penguin.service

import com.devjk.penguin.db.entity.OidcUser
import com.devjk.penguin.db.repository.OidcUserRepository
import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val oidcUserRepository: OidcUserRepository,
) {

    fun getUserOidcProjects(
        user: AuthUser
    ): List<OidcUser> {
        return if (user.authenticated()) {
            oidcUserRepository.findByOwnerId(user.id)
        } else {
            emptyList()
        }
    }

    fun getUserOidcProject(
        user: AuthUser,
        oidcId: Long
    ): OidcUser {
        return oidcUserRepository.findByIdAndOwnerId(oidcId, user.id)
            ?: throw BaseException(ErrorCode.OIDC_PROJECT_NOT_FOUND, "접근할 수 없습니다.")
    }

    fun createOidcProject(
        user: AuthUser,
        projectName: String,
        redirectUris: List<String>
    ): Pair<OidcUser, String> {
        val oidcUser = OidcUser.create(projectName, redirectUris, user.id)
        val savedUser = oidcUserRepository.save(oidcUser.first)
        return Pair(savedUser, oidcUser.second)
    }
}