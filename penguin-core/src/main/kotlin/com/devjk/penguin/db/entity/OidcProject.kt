package com.devjk.penguin.db.entity

import com.devjk.penguin.framework.common.BaseEntity
import jakarta.persistence.*
import org.apache.commons.lang3.RandomStringUtils
import java.io.Serializable
import java.security.MessageDigest

@Entity
@Table(name = "\"oidc_project\"")
@EntityListeners
class OidcProject(
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

    companion object {
        fun create(
            projectName: String,
            redirectUris: List<String>,
            ownerId: Long,
        ): Pair<OidcProject, String> {
            val clientId = "penguin-${
                RandomStringUtils.secure().nextAlphanumeric(10)
            }-${System.currentTimeMillis()}"
            val clientSecret = RandomStringUtils.secure().nextAlphanumeric(32)
            val clientSecretHashed = MessageDigest.getInstance("SHA-256").apply {
                update(clientSecret.toByteArray())
            }.digest().joinToString("") {
                "%02x".format(it)
            }

            val oidc = OidcProject(
                clientId = clientId,
                clientSecret = clientSecretHashed,
                projectName = projectName,
                ownerId = ownerId,
                redirectUris = redirectUris.joinToString(",")
            )

            return Pair(oidc, clientSecret)
        }
    }

    fun isNotMatchedUser(redirectUri: String, scope: String): Boolean {
        val uri = if (redirectUri.contains("://")) {
            redirectUri.substring(redirectUri.indexOf("://") + 3)
        } else {
            redirectUri
        }

        return !(redirectUris.split(",").contains(uri) && scopes.split(",").contains(scope))
    }
}