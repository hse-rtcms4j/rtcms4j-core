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
        httpStatus.value(),
        httpStatus.reasonPhrase,
    ).also { api ->
        api.detailCode = detailCode
        api.detailMessage = detailMessage
    },
    httpStatus,
)
