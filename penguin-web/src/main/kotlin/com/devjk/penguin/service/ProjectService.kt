package com.devjk.penguin.service

import com.devjk.penguin.db.entity.OidcProject
import com.devjk.penguin.db.repository.OidcProjectRepository
import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val oidcProjectRepository: OidcProjectRepository,
) {

    fun getUserOidcProjects(
        user: AuthUser
    ): List<OidcProject> {
        return if (user.authenticated()) {
            oidcProjectRepository.findByOwnerId(user.id)
        } else {
            emptyList()
        }
    }

    fun getUserOidcProject(
        user: AuthUser,
        oidcId: Long
    ): OidcProject {
        return oidcProjectRepository.findByIdAndOwnerId(oidcId, user.id)
            ?: throw BaseException(ErrorCode.OIDC_PROJECT_NOT_FOUND, "접근할 수 없습니다.")
    }

    fun createOidcProject(
        user: AuthUser,
        projectName: String,
        redirectUris: List<String>
    ): Pair<OidcProject, String> {
        val oidcProject = OidcProject.create(projectName, redirectUris, user.id)

        oidcProjectRepository.findByOwnerIdAndProjectName(user.id, projectName)?.let {
            throw BaseException(ErrorCode.OIDC_PROJECT_ALREADY_EXISTS, "이미 같은 이름의 프로젝트가 있어요")
        }

        val savedUser = oidcProjectRepository.save(oidcProject.first)
        return Pair(savedUser, oidcProject.second)
    }
}