package com.devjk.penguin.db.entity

import com.devjk.penguin.domain.oidc.OidcProvisionStatus
import com.devjk.penguin.framework.common.BaseEntity
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "\"user_oidc_provision\"")
@EntityListeners
class UserOidcProvision(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "project_id")
    val projectId: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: OidcProvisionStatus

) : BaseEntity(), Serializable {

    companion object {
        fun create(
            userId: Long,
            projectId: Long
        ): UserOidcProvision {
            val code = "${userId}-${UUID.randomUUID().toString().replace("-", "")}"

            return UserOidcProvision(
                userId = userId,
                projectId = projectId,
                code = code,
                status = OidcProvisionStatus.WAITING
            )
        }
    }

    fun isActive(): Boolean {
        return this.status == OidcProvisionStatus.ACTIVE
    }

    fun isCodeUsed(): Boolean {
        return this.status != OidcProvisionStatus.WAITING
    }

    fun waitForAuthenticated() {
        if (this.isActive()) {
            throw BaseException(ErrorCode.INVALID_REQUEST, "Already active provision")
        }

        this.status = OidcProvisionStatus.WAITING
    }

    fun activate() {
        this.status = OidcProvisionStatus.ACTIVE
    }
}