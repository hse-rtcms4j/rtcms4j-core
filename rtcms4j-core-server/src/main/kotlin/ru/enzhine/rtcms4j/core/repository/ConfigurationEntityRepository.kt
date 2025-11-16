package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity
import kotlin.jvm.Throws

interface ConfigurationEntityRepository {
    /**
     * @throws DuplicateKeyException name duplication
     * @throws DataIntegrityViolationException application does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(configurationEntity: ConfigurationEntity): ConfigurationEntity

    fun findAllByApplicationIdAndName(
        applicationId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ConfigurationEntity>

    fun findById(id: Long): ConfigurationEntity?

    fun removeById(id: Long): Boolean
}
