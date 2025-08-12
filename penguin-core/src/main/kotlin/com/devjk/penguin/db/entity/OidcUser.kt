package com.devjk.penguin.db.entity

import com.devjk.penguin.framework.common.BaseEntity
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "\"oidc_user\"")
@EntityListeners
class OidcUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "client_id")
    val clientId: String,

    @Column(name = "client_secret")
    val clientSecret: String,

    @Column(name = "project_name")
    val projectName: String,

    @Column(name = "owner_id")
    val ownerId: Long,

    @Column(name = "redirect_uris")
    val redirectUris: String = "",

    @Column(name = "scopes")
    val scopes: String = "openid"
) : BaseEntity(), Serializable {
}