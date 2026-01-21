package ru.enzhine.rtcms4j.core.service.external

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.ClientRepresentation
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.config.CacheConfig
import ru.enzhine.rtcms4j.core.config.props.KeycloakProperties
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakClient
import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakUser
import java.util.UUID

@Service
class KeycloakServiceImpl(
    private val keycloakAdminClient: Keycloak,
    private val keycloakProperties: KeycloakProperties,
) : KeycloakService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun isUserExists(subject: UUID): Boolean {
        try {
            keycloakAdminClient
                .realm(keycloakProperties.realm)
                .users()
                .get(subject.toString())
                .toRepresentation()

            return true
        } catch (_: Throwable) {
            return false
        }
    }

    @Cacheable(
        cacheNames = [CacheConfig.KEYCLOAK_USERS_CACHE],
        unless = "#result == null",
    )
    override fun getUserOrCache(subject: UUID): KeycloakUser? {
        try {
            val userRepresentation =
                keycloakAdminClient
                    .realm(keycloakProperties.realm)
                    .users()
                    .get(subject.toString())
                    .toRepresentation()

            return KeycloakUser(
                subject = subject,
                username = userRepresentation.username,
                firstName = userRepresentation.firstName,
                lastName = userRepresentation.lastName,
            )
        } catch (_: Throwable) {
            return null
        }
    }

    override fun buildClientId(
        namespaceId: Long,
        applicationId: Long,
    ) = "ns${namespaceId}_app$applicationId"

    override fun findApplicationClient(clientId: String): KeycloakClient {
        val clientRepresentation =
            keycloakAdminClient
                .realm(keycloakProperties.realm)
                .clients()
                .findByClientId(clientId)
                .first()

        return KeycloakClient(
            sub = UUID.fromString(clientRepresentation.id),
            clientId = clientRepresentation.clientId,
            clientSecret = clientRepresentation.secret,
        )
    }

    override fun createNewApplicationClient(
        namespaceId: Long,
        applicationId: Long,
    ): KeycloakClient {
        val clientId = buildClientId(namespaceId, applicationId)
        val clientRepresentation = buildClientRepresentation(namespaceId, applicationId)

        val response =
            keycloakAdminClient
                .realm(keycloakProperties.realm)
                .clients()
                .create(clientRepresentation)

        return when (response.status) {
            in 200 until 300 -> findApplicationClient(clientId)
            409 -> throw ConditionFailureException(
                message = "Keycloak already contains client with id '$clientId'",
                cause = null,
                detailCode = response.status,
            )

            else -> throw RuntimeException("Keycloak client creation failed.")
        }
    }

    override fun rotateApplicationClientPassword(clientId: String): KeycloakClient {
        val keycloakClient = findApplicationClient(clientId)

        val credentialRepresentation =
            keycloakAdminClient
                .realm(keycloakProperties.realm)
                .clients()
                .get(keycloakClient.sub.toString())
                .generateNewSecret()

        return KeycloakClient(
            sub = keycloakClient.sub,
            clientId = keycloakClient.clientId,
            clientSecret = credentialRepresentation.value,
        )
    }

    override fun deleteApplicationClient(clientId: String): Boolean {
        val keycloakClient = findApplicationClient(clientId)

        val response =
            keycloakAdminClient
                .realm(keycloakProperties.realm)
                .clients()
                .delete(keycloakClient.sub.toString())

        return when (response.status) {
            in 200 until 300 -> true

            404 -> throw ConditionFailureException(
                message = "Keycloak does not contain client with id '$clientId'",
                cause = null,
                detailCode = response.status,
            )

            else -> throw RuntimeException("Keycloak client deletion failed.")
        }
    }

    private var onceTested = false

    @EventListener
    fun onStartup(event: ContextRefreshedEvent) {
        if (!onceTested) {
            testConnection()
        }
    }

    private fun testConnection() =
        try {
            val realm = keycloakAdminClient.realm(keycloakProperties.realm).toRepresentation()
            logger.info("Keycloak connection passed. Serving realm: ${realm.realm}.")
        } catch (ex: Throwable) {
            throw RuntimeException("Application startup failed: keycloak connection error!", ex)
        }

    private fun buildClientRepresentation(
        namespaceId: Long,
        applicationId: Long,
    ) = ClientRepresentation().apply {
        this.clientId = buildClientId(namespaceId, applicationId)
        this.name = "Application client $clientId"
        this.attributes =
            mapOf(
                keycloakProperties.resourcePrefix + "NAMESPACE_ID" to namespaceId.toString(),
                keycloakProperties.resourcePrefix + "APPLICATION_ID" to applicationId.toString(),
            )
        this.isPublicClient = false
        this.isServiceAccountsEnabled = true
        this.isDirectAccessGrantsEnabled = false
        this.isStandardFlowEnabled = false
        this.isEnabled = true
    }
}
