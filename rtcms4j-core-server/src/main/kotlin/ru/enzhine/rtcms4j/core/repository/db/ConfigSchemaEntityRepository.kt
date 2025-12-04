package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigSchemaEntity
import kotlin.jvm.Throws

interface ConfigSchemaEntityRepository {
    /**
     * @throws DuplicateKeyException schema duplication
     * @throws DataIntegrityViolationException configuration does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(configSchemaDetailedEntity: ConfigSchemaDetailedEntity): ConfigSchemaDetailedEntity

    fun findAllByConfigurationId(configurationId: Long): List<ConfigSchemaEntity>

    fun findById(id: Long): ConfigSchemaDetailedEntity?

    fun removeById(id: Long): Boolean
}
