package ru.enzhine.rtcms4j.core.service.internal

import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.config.props.RolesConfig
import ru.enzhine.rtcms4j.core.security.dto.KeycloakPrincipal
import ru.enzhine.rtcms4j.core.service.external.KeycloakService

@Service
class AccessControlServiceImpl(
    private val rolesConfig: RolesConfig,
    private val namespaceService: NamespaceService,
    private val applicationService: ApplicationService,
    private val keycloakService: KeycloakService,
) : AccessControlService {
    override fun hasAccessToAllNamespaces(keycloakPrincipal: KeycloakPrincipal): Boolean =
        keycloakPrincipal.roles.contains(rolesConfig.superAdminRole)

    override fun hasAccessToNamespace(
        keycloakPrincipal: KeycloakPrincipal,
        namespaceId: Long,
    ): Boolean =
        hasAccessToAllNamespaces(keycloakPrincipal) ||
            falseOnException {
                namespaceService.listAdmins(namespaceId).contains(keycloakPrincipal.sub)
            }

    override fun hasAccessToApplication(
        keycloakPrincipal: KeycloakPrincipal,
        namespaceId: Long,
        applicationId: Long,
    ): Boolean =
        hasAccessToNamespace(keycloakPrincipal, namespaceId) ||
            falseOnException {
                applicationService.listManagers(namespaceId, applicationId).contains(keycloakPrincipal.sub)
            }

    override fun hasAccessToConfigurations(
        keycloakPrincipal: KeycloakPrincipal,
        namespaceId: Long,
        applicationId: Long,
    ): Boolean =
        if (keycloakPrincipal.isClient) {
            keycloakPrincipal.clientId == keycloakService.buildClientId(namespaceId, applicationId)
        } else {
            hasAccessToApplication(keycloakPrincipal, namespaceId, applicationId)
        }

    private fun falseOnException(block: () -> Boolean) =
        try {
            block()
        } catch (_: Exception) {
            false
        }
}
