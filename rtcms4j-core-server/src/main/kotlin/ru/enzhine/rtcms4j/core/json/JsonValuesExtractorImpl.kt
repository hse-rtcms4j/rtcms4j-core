package ru.enzhine.rtcms4j.core.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.builder.jsonValuesVersionException

@Component
class JsonValuesExtractorImpl(
    private val objectMapper: ObjectMapper,
    @param:Value($$"${json.version-field.name}")
    private val versionFieldName: String,
    @param:Value($$"${json.version-field.pattern}")
    private val versionFieldPatternString: String,
) : JsonValuesExtractor {
    private val versionFieldPattern = Regex(versionFieldPatternString)

    override fun validateAndGetVersion(jsonValues: String): String {
        val rootNode = objectMapper.readTree(jsonValues)
        val versionNode = rootNode.get(versionFieldName)

        if (versionNode == null) {
            throw jsonValuesVersionException("Json values must contain '$versionFieldName' field.")
        }

        if (versionNode is NullNode) {
            throw jsonValuesVersionException("Json values field '$versionFieldName' must not be null.")
        }

        if (!versionNode.isTextual) {
            throw jsonValuesVersionException("Json values field '$versionFieldName' must contain string value.")
        }
        val version = versionNode.textValue()

        if (!versionFieldPattern.matches(version)) {
            throw jsonValuesVersionException("Json values field '$versionFieldName' must match '$versionFieldPatternString' pattern.")
        }

        return version
    }
}
