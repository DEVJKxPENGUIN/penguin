package com.devjk.penguin.db.repository

import com.devjk.penguin.db.entity.UserOidcProvision
import com.devjk.penguin.domain.oidc.OidcProvisionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserOidcProvisionRepository : JpaRepository<UserOidcProvision, Long> {


    fun findByUserIdAndProjectIdAndStatus(userId: Long, projectId: Long, status: OidcProvisionStatus): UserOidcProvision?

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): UserOidcProvision?
}