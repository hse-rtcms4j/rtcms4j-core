package ru.enzhine.rtcms4j.core.service.internal

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.enzhine.rtcms4j.core.builder.newOutboxTaskKcClientEntity
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.repository.db.OutboxTaskKcClientEntityRepository
import ru.enzhine.rtcms4j.core.repository.db.dto.OutboxTaskKcClientEntity
import ru.enzhine.rtcms4j.core.service.external.KeycloakService

@Service
class KeycloakOutboxServiceImpl(
    private val keycloakService: KeycloakService,
    private val outboxTaskKcClientEntityRepository: OutboxTaskKcClientEntityRepository,
    @param:Value($$"${job.keycloak-retry.batch-size}")
    private val batchSize: Int,
    @param:Value($$"${job.keycloak-retry.max-attempts}")
    private val maxAttempts: Int,
) : KeycloakOutboxService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun createKeycloakApplicationGuaranteed(
        namespaceId: Long,
        applicationId: Long,
    ) {
        val retry =
            try {
                keycloakService.createNewApplicationClient(namespaceId, applicationId)

                false // do not retry on success
            } catch (ex: ConditionFailureException) {
                logger.error(
                    "Unable to create Keycloak client for application with id $applicationId.",
                    ex,
                )

                false // do not retry on wrong input
            } catch (ex: Throwable) {
                logger.error(
                    "Unable to create Keycloak client for application with id $applicationId. Storing retry task in outbox.",
                    ex,
                )

                true // retry on failure
            }

        if (retry) {
            createInitialTaskKcClientEntity(true, namespaceId, applicationId)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun deleteKeycloakApplicationGuaranteed(
        namespaceId: Long,
        applicationId: Long,
    ) {
        val retry =
            try {
                keycloakService.deleteApplicationClient(namespaceId, applicationId)

                false // do not retry on success
            } catch (ex: ConditionFailureException) {
                logger.error(
                    "Unable to delete Keycloak client for application with id $applicationId.",
                    ex,
                )

                false // do not retry on wrong input
            } catch (ex: Throwable) {
                logger.error(
                    "Unable to delete Keycloak client for application with id $applicationId. Storing retry task in outbox.",
                    ex,
                )

                true // retry on failure
            }

        if (retry) {
            createInitialTaskKcClientEntity(false, namespaceId, applicationId)
        }
    }

    @Transactional
    @Scheduled(cron = $$"${job.keycloak-retry.cron}")
    fun attemptRetry() {
        logger.info("Executing Keycloak client outbox tasks.")
        outboxTaskKcClientEntityRepository
            .findBatchForUpdateSkipLocked(batchSize, maxAttempts)
            .forEach { task ->
                val retry =
                    when (task.action) {
                        OutboxTaskKcClientEntity.Action.CREATE ->
                            try {
                                val client = keycloakService.createNewApplicationClient(task.namespaceId, task.applicationId)
                                logger.info("Created Keycloak client ${client.clientId} for application with id ${task.applicationId}.")

                                false // do not retry on success
                            } catch (ex: ConditionFailureException) {
                                logger.error(
                                    "Outbox Keycloak client creation for application with id ${task.applicationId} attempt ${task.attempt} failed.",
                                    ex,
                                )

                                false // do not retry on wrong input
                            } catch (ex: Throwable) {
                                logger.error(
                                    "Outbox Keycloak client creation for application with id ${task.applicationId} attempt ${task.attempt} failed. Pending for reattempt.",
                                    ex,
                                )

                                true // retry on failure
                            }

                        OutboxTaskKcClientEntity.Action.DELETE ->
                            try {
                                keycloakService.deleteApplicationClient(task.namespaceId, task.applicationId)
                                logger.info("Deleted Keycloak client for application with id ${task.applicationId}.")

                                false // do not retry on success
                            } catch (ex: ConditionFailureException) {
                                logger.error(
                                    "Outbox Keycloak client creation for application with id ${task.applicationId} attempt ${task.attempt} failed.",
                                    ex,
                                )

                                false // do not retry on wrong input
                            } catch (ex: Throwable) {
                                logger.error(
                                    "Outbox Keycloak client creation for application with id ${task.applicationId} attempt ${task.attempt} failed. Pending for reattempt.",
                                    ex,
                                )

                                true // retry on failure
                            }
                    }

                if (retry) {
                    task.attempt++
                    if (task.attempt >= maxAttempts) {
                        task.skip = true
                    }
                    outboxTaskKcClientEntityRepository.updateAttemptAndSkipProperties(task)
                } else {
                    outboxTaskKcClientEntityRepository.removeById(task.id)
                }
            }
    }

    private fun createInitialTaskKcClientEntity(
        createElseDelete: Boolean,
        namespaceId: Long,
        applicationId: Long,
    ): OutboxTaskKcClientEntity =
        outboxTaskKcClientEntityRepository.save(
            newOutboxTaskKcClientEntity(
                attempt = 1,
                skip = false,
                createElseDelete = createElseDelete,
                namespaceId = namespaceId,
                applicationId = applicationId,
            ),
        )
}
