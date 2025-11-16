package ru.enzhine.rtcms4j.core.builder

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import ru.enzhine.rtcms4j.core.api.dto.ErrorResponse

fun newErrorResponseEntity(
    httpStatus: HttpStatus,
    detailCode: Int? = null,
    detailMessage: String? = null,
) = ResponseEntity(
    ErrorResponse(
        httpCode = httpStatus.value(),
        httpStatus = httpStatus.reasonPhrase,
        detailCode = detailCode,
        detailMessage = detailMessage,
    ),
    httpStatus,
)
