package ru.enzhine.rtcms4j.core.json

interface JsonSchemaValidator {
    fun validateSchema(jsonSchema: String)

    fun validateValuesBySchema(
        jsonValues: String,
        jsonSchema: String,
    )
}
