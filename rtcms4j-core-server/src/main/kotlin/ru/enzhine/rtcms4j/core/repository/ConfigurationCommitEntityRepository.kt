package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitDetailedEntity
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitEntity
import kotlin.jvm.Throws

interface ConfigurationCommitEntityRepository {
    /**
     * @throws DuplicateKeyException commit hash duplication
     * @throws DataIntegrityViolationException configuration does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(configurationCommitDetailedEntity: ConfigurationCommitDetailedEntity): ConfigurationCommitDetailedEntity

    fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitEntity>

    fun findByConfigurationIdAndCommitHash(
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitDetailedEntity?

    fun findById(id: Long): ConfigurationCommitDetailedEntity?

    fun removeById(id: Long): Boolean
}
