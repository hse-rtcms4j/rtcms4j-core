package ru.enzhine.rtcms4j.core.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakRealmConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority>? {
        val authorities = mutableListOf<GrantedAuthority>()

        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val realmRoles = realmAccess?.get("roles") as? Collection<*>

        realmRoles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
        val clientAccess = resourceAccess?.get("my-api") as? Map<*, *>
        val clientRoles = clientAccess?.get("roles") as? Collection<*>

        clientRoles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        return authorities
    }
}
