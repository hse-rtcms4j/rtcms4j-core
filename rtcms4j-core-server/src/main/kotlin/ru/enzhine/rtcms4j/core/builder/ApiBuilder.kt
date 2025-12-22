package ru.enzhine.rtcms4j.core.builder

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.enzhine.rtcms4j.core.api.dto.ErrorResponseDto

fun newErrorResponseEntity(
    httpStatus: HttpStatus,
    detailCode: Int? = null,
    detailMessage: String? = null,
) = ResponseEntity(
    ErrorResponseDto(
        httpCode = httpStatus.value(),
        httpStatus = httpStatus.reasonPhrase,
        detailCode = detailCode,
        detailMessage = detailMessage,
    ),
    httpStatus,
)
