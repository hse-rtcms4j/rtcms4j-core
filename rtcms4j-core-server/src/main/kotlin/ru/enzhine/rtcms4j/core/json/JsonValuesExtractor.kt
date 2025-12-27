package ru.enzhine.rtcms4j.core.json

interface JsonValuesExtractor {
    fun validateAndGetVersion(jsonValues: String): String
}
