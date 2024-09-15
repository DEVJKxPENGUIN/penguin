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
    ;
}