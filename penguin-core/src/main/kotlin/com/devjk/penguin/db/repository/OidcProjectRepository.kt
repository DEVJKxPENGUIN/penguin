package com.devjk.penguin.db.repository

import com.devjk.penguin.db.entity.OidcProject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OidcProjectRepository : JpaRepository<OidcProject, Long> {

    fun findByOwnerId(ownerId: Long): List<OidcProject>

    fun findByIdAndOwnerId(id: Long, ownerId: Long): OidcProject?

    fun findByOwnerIdAndProjectName(ownerId: Long, projectName: String): OidcProject?

    fun findByClientId(clientId: String): OidcProject?
}