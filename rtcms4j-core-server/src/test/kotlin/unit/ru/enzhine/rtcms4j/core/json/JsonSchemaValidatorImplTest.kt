package unit.ru.enzhine.rtcms4j.core.json

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.enzhine.rtcms4j.core.exception.ConditionFailureException
import ru.enzhine.rtcms4j.core.json.JsonSchemaValidatorImpl

class JsonSchemaValidatorImplTest {
    private val jsonSchemaValidator = JsonSchemaValidatorImpl("json/schema/meta-schema.json")

    private val correctValues =
        """
        {
          "featureFlag": true
        }
        """.trimIndent()

    private val wrongValues =
        """
        {
          "key": "value"
        }
        """.trimIndent()

    private val correctSchema =
        $$"""
        {
          "$schema": "https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "additionalProperties": false,
          "properties": {
            "featureFlag": {
              "type": "boolean",
              "default": false
            }
          },
          "required": ["featureFlag"]
        }
        """.trimIndent()

    private val wrongSchema =
        $$"""
        {
          "$schema": "https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "additionalProperties": false,
          "properties": {
            "featureFlag": {
              "type": "boolean",
              "default": false
            },
            "subObject": {
              "type": "object",
              "properties": {
                "key": {
                  "type": "boolean",
                  "default": false
                }
              }
            }
          },
          "required": ["featureFlag", "subObject"]
        }
        """.trimIndent()

    @Test
    fun validateSchema_correctSchema_validateByMetaSchemaPassed() {
        Assertions.assertDoesNotThrow {
            jsonSchemaValidator.validateSchema(correctSchema)
        }
    }

    @Test
    fun validateSchema_wrongSchema_validateByMetaSchemaFailed() {
        Assertions.assertThrows(ConditionFailureException::class.java) {
            jsonSchemaValidator.validateSchema(wrongSchema)
        }
    }

    @Test
    fun validateValuesBySchema_correctValues_validateBySchemaPassed() {
        Assertions.assertDoesNotThrow {
            jsonSchemaValidator.validateValuesBySchema(correctValues, correctSchema)
        }
    }

    @Test
    fun validateValuesBySchema_wrongValues_validateBySchemaFailed() {
        Assertions.assertThrows(ConditionFailureException::class.java) {
            jsonSchemaValidator.validateValuesBySchema(wrongValues, correctSchema)
        }
    }
}
