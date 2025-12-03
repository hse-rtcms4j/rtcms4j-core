package ru.enzhine.rtcms4j.core.json

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64

@Component
class CommitHashEvaluatorImpl : CommitHashEvaluator {
    private val threadLocalSha256 =
        ThreadLocal.withInitial {
            MessageDigest.getInstance("SHA-256")
        }

    override fun evalCommitHash(
        jsonValues: String,
        jsonSchema: String,
    ): String {
        val input = (jsonValues + jsonSchema).toByteArray(Charsets.UTF_8)

        val sha256 =
            threadLocalSha256
                .get()
                .apply {
                    reset()
                }

        val output = sha256.digest(input)
        return Base64.getUrlEncoder().encodeToString(output)
    }
}
