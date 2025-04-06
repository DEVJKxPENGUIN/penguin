package com.devjk.penguin.db.repository

import com.devjk.penguin.db.entity.User
import com.devjk.penguin.domain.oidc.OidcProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByProviderAndEmail(provider: OidcProvider, email: String): User?
}