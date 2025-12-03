package ru.enzhine.rtcms4j.core.json

interface CommitHashEvaluator {
    fun evalCommitHash(
        jsonValues: String,
        jsonSchema: String,
    ): String
}
