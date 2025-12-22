package ru.enzhine.rtcms4j.core.handler

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.enzhine.rtcms4j.core.api.dto.ErrorResponseDto
import ru.enzhine.rtcms4j.core.builder.newErrorResponseEntity
import ru.enzhine.rtcms4j.core.controller.CoreController
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException

@RestControllerAdvice(basePackageClasses = [CoreController::class])
class ExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(value = [ConditionFailureException::class])
    fun conditionFailureExceptionHandler(ex: ConditionFailureException): ResponseEntity<ErrorResponseDto> {
        logger.warn("ConditionFailureException reached.", ex)
        return when (ex) {
            is ConditionFailureException.NotFound ->
                newErrorResponseEntity(
                    HttpStatus.NOT_FOUND,
                    ex.detailCode,
                    ex.message,
                )

            is ConditionFailureException.KeyDuplicated ->
                newErrorResponseEntity(
                    HttpStatus.CONFLICT,
                    ex.detailCode,
                    ex.message,
                )

            is ConditionFailureException.ForbiddenAccess ->
                newErrorResponseEntity(
                    HttpStatus.FORBIDDEN,
                    ex.detailCode,
                    ex.message,
                )

            else -> newErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ExceptionHandler
    fun anyHandler(ex: Throwable): ResponseEntity<ErrorResponseDto> {
        logger.error("An unknown exception handled!", ex)
        return newErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
