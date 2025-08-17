package com.devjk.penguin.db.repository

import com.devjk.penguin.db.entity.OidcUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OidcUserRepository : JpaRepository<OidcUser, Long> {

    fun findByOwnerId(ownerId: Long): List<OidcUser>

    fun findByIdAndOwnerId(id: Long, ownerId: Long): OidcUser?
}