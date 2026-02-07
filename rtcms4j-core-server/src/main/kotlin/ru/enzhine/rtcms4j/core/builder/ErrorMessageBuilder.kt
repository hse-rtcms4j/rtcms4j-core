package ru.enzhine.rtcms4j.core.builder

import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.HandlerMethodValidationException
import kotlin.let

fun humanizeError(ex: MethodArgumentNotValidException) = ex.fieldError?.let { "${it.field}: ${it.defaultMessage}." } ?: ""

fun humanizeError(ex: HandlerMethodValidationException) = ex.allErrors.mapNotNull { it.defaultMessage }.joinToString(", ")
