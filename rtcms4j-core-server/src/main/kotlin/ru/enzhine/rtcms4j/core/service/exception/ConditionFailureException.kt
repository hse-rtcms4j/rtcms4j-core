package ru.enzhine.rtcms4j.core.service.exception

import org.springframework.dao.DuplicateKeyException
import java.lang.RuntimeException

open class ConditionFailureException(
    message: String,
    cause: Throwable?,
) : RuntimeException(message, cause) {
    class KeyDuplicated(
        keyName: String,
        cause: DuplicateKeyException,
    ) : ConditionFailureException("$keyName should be unique", cause)

    class NotFound(
        entity: String,
        id: Long,
    ) : ConditionFailureException("$entity with id $id not found", null)
}
