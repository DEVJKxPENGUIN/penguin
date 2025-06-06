package com.devjk.penguin.framework.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val value: Int,
    val message: String,
) {
    //format: off
    UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, -1, "Unknown Error occurred"),
    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, -2, "Internal Server Error"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, -3, "cannot find valid token"),
    NO_AUTHORIZED_ROLE(HttpStatus.FORBIDDEN, -4, "no authorized role"),
    INVALID_STATETOKEN(HttpStatus.BAD_REQUEST, -5, "invalid state token"),
    UNREGISTERED_USER(HttpStatus.UNAUTHORIZED, -6, "unregistered user"),
    INVALID_OIDC_PROVIDER(HttpStatus.BAD_REQUEST, -7, "invalid oidc provider"),
    INVALID_SIGNUP_ACCESS(HttpStatus.BAD_REQUEST, -8, "invalid signup access"),
    OIDC_PROVIDER_AUTH_FAIL(HttpStatus.BAD_REQUEST, -9, "oidc provider auth fail"),
    ;
}