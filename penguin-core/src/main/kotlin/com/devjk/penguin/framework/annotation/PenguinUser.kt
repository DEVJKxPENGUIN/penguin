package com.devjk.penguin.framework.annotation

import com.devjk.penguin.domain.auth.Role

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PenguinUser(
    val min: Role
)
