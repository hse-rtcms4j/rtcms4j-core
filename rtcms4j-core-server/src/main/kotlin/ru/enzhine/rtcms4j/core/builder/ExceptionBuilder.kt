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
    commitId: Long,
    detailCode: Int? = null,
) = ConditionFailureException.NotFound(
    message = "Commit with id '$commitId' for configuration with id '$configurationId' not found.",
    detailCode = detailCode,
)

fun nameKeyDuplicatedException(
    cause: DuplicateKeyException,
    detailCode: Int? = null,
) = ConditionFailureException.KeyDuplicated("name should be unique.", cause, detailCode)

fun configSchemaDuplicatedException(
    cause: DuplicateKeyException,
    configurationId: Long,
    detailCode: Int? = null,
) = ConditionFailureException.KeyDuplicated(
    message = "Config schema for configuration with id '$configurationId' already posted.",
    cause = cause,
    detailCode = detailCode,
)

fun configValuesDuplicatedException(
    cause: DuplicateKeyException,
    configurationId: Long,
    detailCode: Int? = null,
) = ConditionFailureException.KeyDuplicated(
    message = "Config values for configuration with id '$configurationId' already posted.",
    cause = cause,
    detailCode = detailCode,
)
