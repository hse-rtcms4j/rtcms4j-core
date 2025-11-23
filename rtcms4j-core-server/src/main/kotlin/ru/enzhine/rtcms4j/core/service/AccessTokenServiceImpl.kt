package ru.enzhine.rtcms4j.core.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64

@Service
class AccessTokenServiceImpl(
    @param:Value($$"${access-token.prefix}")
    private val accessTokenPrefix: String,
    @Value($$"${random.seed:null}")
    randomSeed: String?,
) : AccessTokenService {
    private val secureRandom =
        if (randomSeed != null) {
            SecureRandom(randomSeed.toByteArray())
        } else {
            SecureRandom()
        }

    override fun randomAccessToken(): String {
        val ba = ByteArray(32)
        secureRandom.nextBytes(ba)
        return accessTokenPrefix + Base64.getMimeEncoder().encodeToString(ba)
    }
}
