package ru.enzhine.rtcms4j.core.service.external

import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.ClientRepresentation
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.config.props.KeycloakAdminProperties
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.service.external.dto.ApplicationClient

@Service
class KeycloakServiceImpl(
    private val keycloakAdminClient: Keycloak,
    private val keycloakAdminProperties: KeycloakAdminProperties,
) : KeycloakService {
    override fun buildClientId(
        namespaceId: Long,
        applicationId: Long,
    ) = "ns${namespaceId}_app$applicationId"

    override fun findApplicationClient(clientId: String): ApplicationClient {
        val clientResource =
            keycloakAdminClient
                .realm(keycloakAdminProperties.realm)
                .clients()
                .get(clientId)

        return ApplicationClient(
            clientId = clientId,
            clientSecret = clientResource.secret.value,
        )
    }

    override fun createNewApplicationClient(clientId: String): ApplicationClient {
        val clientRepresentation = buildClientRepresentation(clientId)

        val response =
            keycloakAdminClient
                .realm(keycloakAdminProperties.realm)
                .clients()
                .create(clientRepresentation)

        return when (response.status) {
            in 200 until 300 -> findApplicationClient(clientId)
            409 -> throw ConditionFailureException(
                message = "Keycloak already contains client with id '$clientId'",
                cause = null,
                detailCode = response.status,
            )

            else -> throw RuntimeException("Keycloak application client creation failed.")
        }
    }

    override fun rotateApplicationClientPassword(clientId: String): ApplicationClient {
        val credentialRepresentation =
            keycloakAdminClient
                .realm(keycloakAdminProperties.realm)
                .clients()
                .get(clientId)
                .generateNewSecret()

        return ApplicationClient(
            clientId = clientId,
            clientSecret = credentialRepresentation.value,
        )
    }

    private fun buildClientRepresentation(clientId: String) =
        ClientRepresentation().apply {
            this.clientId = clientId
            this.name = "Application client $clientId"

            this.isPublicClient = false
            this.isServiceAccountsEnabled = true
            this.isDirectAccessGrantsEnabled = false
            this.isStandardFlowEnabled = false
            this.isEnabled = true
        }
}
