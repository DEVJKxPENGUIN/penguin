package com.penguin.penguincore.framework.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.penguin.penguincore.framework.error.ErrorCode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun success(): BaseResponse<Unit> {
            return BaseResponse("0", "OK")
        }

        fun <T> success(data: T): BaseResponse<T> {
            return BaseResponse("0", "OK", data)
        }

        fun error(errorCode: ErrorCode, message: String): BaseResponse<Unit> {
            return BaseResponse(errorCode.value.toString(), message)
        }
    }
}