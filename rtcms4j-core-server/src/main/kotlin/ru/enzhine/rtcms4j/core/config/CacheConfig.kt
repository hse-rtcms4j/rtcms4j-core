package ru.enzhine.rtcms4j.core.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.ehcache.jsr107.Eh107Configuration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.enzhine.rtcms4j.core.service.external.dto.KeycloakUser
import java.time.Duration
import java.util.UUID
import javax.cache.CacheManager
import javax.cache.Caching

@Configuration
@EnableCaching
class CacheConfig {
    companion object {
        const val KEYCLOAK_USERS_CACHE = "keycloak-users"
    }

    @Bean(destroyMethod = "close")
    fun jCacheManager(): CacheManager {
        val cacheProvider = Caching.getCachingProvider()
        val cacheManager = cacheProvider.cacheManager

        cacheManager.createCache(
            KEYCLOAK_USERS_CACHE,
            Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(
                        UUID::class.java,
                        KeycloakUser::class.java,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1, MemoryUnit.MB),
                    ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))),
            ),
        )

        return cacheManager
    }
}
