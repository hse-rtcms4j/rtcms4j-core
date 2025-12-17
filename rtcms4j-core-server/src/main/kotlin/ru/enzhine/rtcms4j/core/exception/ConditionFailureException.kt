package ru.enzhine.rtcms4j.core.exception

import org.springframework.dao.DuplicateKeyException
import java.lang.RuntimeException

open class ConditionFailureException(
    message: String,
    cause: Throwable?,
    val detailCode: Int?,
) : RuntimeException(message, cause) {
    class KeyDuplicated(
        message: String,
        cause: DuplicateKeyException,
        detailCode: Int?,
    ) : ConditionFailureException(message, cause, detailCode)

    class NotFound(
        message: String,
        detailCode: Int?,
    ) : ConditionFailureException(message, null, detailCode)

    class ForbiddenAccess(
        message: String,
        detailCode: Int?,
    ) : ConditionFailureException(message, null, detailCode)
}
