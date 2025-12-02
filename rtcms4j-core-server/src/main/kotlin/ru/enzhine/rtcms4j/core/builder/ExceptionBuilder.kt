package ru.enzhine.rtcms4j.core.builder

import org.springframework.dao.DuplicateKeyException
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException

fun namespaceNotFoundException(
    namespaceId: Long,
    detailCode: Int? = null,
) = ConditionFailureException.NotFound("Namespace with id '$namespaceId' not found.", detailCode)

fun applicationNotFoundException(
    applicationId: Long,
    detailCode: Int? = null,
) = ConditionFailureException.NotFound("Application with id '$applicationId' not found.", detailCode)

fun configurationNotFoundException(
    configurationId: Long,
    detailCode: Int? = null,
) = ConditionFailureException.NotFound("Configuration with id '$configurationId' not found.", detailCode)

fun configurationCommitNotFoundException(
    configurationId: Long,
    commitHash: String,
    detailCode: Int? = null,
) = ConditionFailureException.NotFound(
    message = "Commit with hash '$commitHash' for configuration with id '$configurationId' not found.",
    detailCode = detailCode,
)

fun nameKeyDuplicatedException(
    cause: DuplicateKeyException,
    detailCode: Int? = null,
) = ConditionFailureException.KeyDuplicated("name should be unique.", cause, detailCode)
