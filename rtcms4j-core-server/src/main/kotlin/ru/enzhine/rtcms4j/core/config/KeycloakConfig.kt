package ru.enzhine.rtcms4j.core.config

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.enzhine.rtcms4j.core.config.props.KeycloakAdminProperties

@Configuration
class KeycloakConfig {
    @Bean
    fun keycloakAdminClient(keycloakAdminProperties: KeycloakAdminProperties): Keycloak =
        KeycloakBuilder
            .builder()
            .serverUrl(keycloakAdminProperties.serverUrl)
            .realm(keycloakAdminProperties.realm)
            .clientId(keycloakAdminProperties.clientId)
            .grantType(OAuth2Constants.PASSWORD)
            .username(keycloakAdminProperties.username)
            .password(keycloakAdminProperties.password)
            .build()
}
