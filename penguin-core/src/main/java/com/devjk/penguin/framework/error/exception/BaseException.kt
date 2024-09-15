package com.devjk.penguin.framework.error.exception

import com.devjk.penguin.framework.error.ErrorCode

class BaseException(
    val errorCode: ErrorCode,
    val detailMessage: String? = null
) : RuntimeException(detailMessage ?: errorCode.message) {
}