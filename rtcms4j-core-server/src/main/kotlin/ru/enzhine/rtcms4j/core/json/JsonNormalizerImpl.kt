package ru.enzhine.rtcms4j.core.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.stereotype.Component

@Component
class JsonNormalizerImpl : JsonNormalizer {
    private val objectMapper =
        ObjectMapper().apply {
            configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

    private val mapTypeReference = object : TypeReference<Map<String, Any?>>() {}

    @Throws(JsonMappingException::class, JsonProcessingException::class)
    override fun normalize(content: String): String {
        val parsed = objectMapper.readValue(content, mapTypeReference)
        val written = objectMapper.writeValueAsString(parsed)
        return written
    }
}
