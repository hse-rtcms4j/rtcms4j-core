package ru.enzhine.rtcms4j.core.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import ru.enzhine.rtcms4j.core.security.dto.KeycloakPrincipal
import java.util.UUID

class JwtKeycloakPrincipalConverter : Converter<Jwt, AbstractAuthenticationToken> {
    override fun convert(jwt: Jwt): AbstractAuthenticationToken? {
        val authorities = extractAuthorities(jwt)

        val principal =
            KeycloakPrincipal(
                sub = UUID.fromString(jwt.subject),
                username = jwt.getClaimAsString("preferred_username"),
                clientId = jwt.getClaimAsString("azp"),
                roles = authorities.map { it.authority }.toSet(),
                namespaceId = (jwt.claims["namespace_id"] as String?)?.toLong(),
                applicationId = (jwt.claims["application_id"] as String?)?.toLong(),
            )

        return JwtAuthenticationToken(
            jwt,
            authorities,
            principal.username ?: principal.clientId ?: principal.sub.toString(),
        ).apply {
            details = principal
        }
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val roles = mutableSetOf<String>()

        // realm roles
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val realmRoles = realmAccess?.get("roles") as? Collection<*>
        realmRoles?.forEach { roles.add(it.toString()) }

        // client roles
        val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
        resourceAccess
            ?.values
            ?.mapNotNull { it as? Map<*, *> }
            ?.mapNotNull { it["roles"] as? Collection<*> }
            ?.flatten()
            ?.forEach { roles.add(it.toString()) }

        return roles.map { SimpleGrantedAuthority(it) }
    }
}
