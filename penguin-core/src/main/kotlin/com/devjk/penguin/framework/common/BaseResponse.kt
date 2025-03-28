package com.devjk.penguin.framework.common

import com.devjk.penguin.framework.error.ErrorCode
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null
){
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