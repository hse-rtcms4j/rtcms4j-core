package ru.enzhine.rtcms4j.core.config

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.enzhine.rtcms4j.core.config.props.KeycloakProperties

@Configuration
class KeycloakConfig {
    @Bean
    fun keycloakAdminClient(keycloakProperties: KeycloakProperties): Keycloak =
        KeycloakBuilder
            .builder()
            .serverUrl(keycloakProperties.serverUrl)
            .realm(keycloakProperties.realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(keycloakProperties.admin.clientId)
            .clientSecret(keycloakProperties.admin.clientSecret)
            .build()
}
