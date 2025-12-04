package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.db.dto.ConfigCommitEntity
import kotlin.jvm.Throws

interface ConfigCommitEntityRepository {
    /**
     * @throws DuplicateKeyException json values duplication
     * @throws DataIntegrityViolationException configuration does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(configCommitDetailedEntity: ConfigCommitDetailedEntity): ConfigCommitDetailedEntity

    fun findAllByConfigSchemaId(configSchemaId: Long): List<ConfigCommitEntity>

    fun findAllByConfigurationId(configurationId: Long): List<ConfigCommitEntity>

    fun findById(id: Long): ConfigCommitDetailedEntity?

    fun removeById(id: Long): Boolean
}
