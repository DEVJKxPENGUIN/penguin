package com.devjk.penguin.framework.custom

import com.devjk.penguin.framework.common.BaseResponse
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionAdvice {

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(e: BaseException): ResponseEntity<*> {
        return handleResponse(e, e.errorCode, e.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleRequestException(e: MethodArgumentNotValidException): ResponseEntity<*> {
        return handleResponse(e, ErrorCode.INVALID_REQUEST, e.bindingResult.fieldError?.defaultMessage)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<*> {
        return handleResponse(e, ErrorCode.UNKNOWN, "알 수 없는 오류가 발생했습니다.")
    }

    private fun handleResponse(
        e: Exception,
        errorCode: ErrorCode,
        message: String?
    ): ResponseEntity<*> {
        e.printStackTrace()
        return ResponseEntity.status(errorCode.httpStatus)
            .body(BaseResponse.error(errorCode, message ?: errorCode.message))
    }
}