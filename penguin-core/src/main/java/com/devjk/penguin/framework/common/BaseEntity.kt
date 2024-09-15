package com.devjk.penguin.framework.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @CreationTimestamp
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    val modifiedAt: LocalDateTime? = null
}