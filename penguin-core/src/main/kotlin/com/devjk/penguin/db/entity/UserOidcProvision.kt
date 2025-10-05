package com.devjk.penguin.db.entity

import com.devjk.penguin.domain.oidc.OidcProvisionStatus
import com.devjk.penguin.framework.common.BaseEntity
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
    var code: String? = null,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: OidcProvisionStatus = OidcProvisionStatus.WAITING

) : BaseEntity(), Serializable {

    companion object {
        fun create(
            userId: Long,
            projectId: Long
        ): UserOidcProvision {

            return UserOidcProvision(
                userId = userId,
                projectId = projectId,
            )
        }
    }

    fun isActive(): Boolean {
        return this.status == OidcProvisionStatus.ACTIVE
    }

    fun renewCode() {
        this.code = "${this.userId}-${UUID.randomUUID().toString().replace("-", "")}"
    }

    fun activate() {
        this.code = null
        this.status = OidcProvisionStatus.ACTIVE
    }
}