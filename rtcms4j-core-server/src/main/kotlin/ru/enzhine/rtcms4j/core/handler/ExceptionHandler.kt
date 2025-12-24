package ru.enzhine.rtcms4j.core.handler

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import ru.enzhine.rtcms4j.core.api.dto.ErrorResponseDto
import ru.enzhine.rtcms4j.core.builder.humanizeError
import ru.enzhine.rtcms4j.core.builder.newErrorResponseEntity
import ru.enzhine.rtcms4j.core.controller.CoreController
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException

@RestControllerAdvice(basePackageClasses = [CoreController::class])
class ExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(value = [ConditionFailureException::class])
    fun conditionFailureExceptionHandler(ex: ConditionFailureException): ResponseEntity<ErrorResponseDto> =
        when (ex) {
            is ConditionFailureException.NotFound ->
                newErrorResponseEntity(
                    httpStatus = HttpStatus.NOT_FOUND,
                    detailCode = ex.detailCode,
                    detailMessage = ex.message,
                )

            is ConditionFailureException.KeyDuplicated ->
                newErrorResponseEntity(
                    httpStatus = HttpStatus.CONFLICT,
                    detailCode = ex.detailCode,
                    detailMessage = ex.message,
                )

            is ConditionFailureException.ForbiddenAccess ->
                newErrorResponseEntity(
                    httpStatus = HttpStatus.FORBIDDEN,
                    detailCode = ex.detailCode,
                    detailMessage = ex.message,
                )

            else -> newErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun methodArgumentNotValidExceptionHandler(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> =
        newErrorResponseEntity(
            httpStatus = HttpStatus.BAD_REQUEST,
            detailMessage = humanizeError(ex),
        )

    @ExceptionHandler(value = [MethodArgumentTypeMismatchException::class])
    fun methodArgumentTypeMismatchExceptionHandler(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponseDto> =
        newErrorResponseEntity(
            httpStatus = HttpStatus.BAD_REQUEST,
            detailMessage = ex.localizedMessage,
        )

    @ExceptionHandler(value = [HandlerMethodValidationException::class])
    fun handlerMethodValidationExceptionHandler(ex: HandlerMethodValidationException): ResponseEntity<ErrorResponseDto> =
        newErrorResponseEntity(
            httpStatus = HttpStatus.BAD_REQUEST,
            detailMessage = humanizeError(ex),
        )

    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun constraintViolationExceptionHandler(ex: ConstraintViolationException): ResponseEntity<ErrorResponseDto> =
        newErrorResponseEntity(
            httpStatus = HttpStatus.BAD_REQUEST,
            detailMessage = ex.localizedMessage,
        )

    @ExceptionHandler
    fun anyHandler(ex: Throwable): ResponseEntity<ErrorResponseDto> {
        logger.error("An unknown exception handled!", ex)
        return newErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
