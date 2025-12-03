package ru.enzhine.rtcms4j.core.json

interface JsonNormalizer {
    fun normalize(content: String): String
}
