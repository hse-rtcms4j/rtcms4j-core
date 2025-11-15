package ru.enzhine.rtcms4j.core.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.enzhine.rtcms4j.core.api.dto.ErrorResponse
import ru.enzhine.rtcms4j.core.builder.newErrorResponseEntity
import ru.enzhine.rtcms4j.core.controller.CoreController

@RestControllerAdvice(basePackageClasses = [CoreController::class])
class ExceptionHandler {
    @ExceptionHandler
    fun anyHandler(ex: Throwable): ResponseEntity<ErrorResponse> =
        newErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
}
