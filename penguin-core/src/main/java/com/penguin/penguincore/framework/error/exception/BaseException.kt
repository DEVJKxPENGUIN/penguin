package com.penguin.penguincore.framework.error.exception

import com.penguin.penguincore.framework.error.ErrorCode

class BaseException(
    val errorCode: ErrorCode,
    val detailMessage: String? = null
) : RuntimeException(detailMessage ?: errorCode.message) {
}