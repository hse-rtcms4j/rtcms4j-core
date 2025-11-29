package ru.enzhine.rtcms4j.core.repository.kv

import ru.enzhine.rtcms4j.core.service.dto.Configuration

interface KeyValueRepository {
    fun putConfigurationSchema(
        configuration: Configuration,
        schema: String,
    )

    fun getConfigurationSchema(configuration: Configuration): String?

    fun putConfigurationValues(
        configuration: Configuration,
        values: String,
    )

    fun getConfigurationValues(configuration: Configuration): String?
}
