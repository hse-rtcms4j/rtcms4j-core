package ru.enzhine.rtcms4j.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.CorsUtils
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import ru.enzhine.rtcms4j.core.config.props.CorsProperties
import ru.enzhine.rtcms4j.core.security.JwtKeycloakPrincipalConverter

@Configuration
class SecurityConfig {
    @Bean
    @Primary
    fun corsConfiguration(corsProperties: CorsProperties): CorsConfigurationSource {
        val config =
            CorsConfiguration()
                .apply {
                    allowedOrigins = corsProperties.allowedOrigins
                    allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                    allowedHeaders = listOf("Authorization", "Content-Type")
                    allowCredentials = true
                }

        return UrlBasedCorsConfigurationSource()
            .apply {
                registerCorsConfiguration("/**", config)
            }
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        configurationSource: CorsConfigurationSource,
    ): SecurityFilterChain {
        http
            .cors { it.configurationSource(configurationSource) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(CorsUtils::isPreFlightRequest)
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(JwtKeycloakPrincipalConverter())
                }
            }

        return http.build()
    }
}
