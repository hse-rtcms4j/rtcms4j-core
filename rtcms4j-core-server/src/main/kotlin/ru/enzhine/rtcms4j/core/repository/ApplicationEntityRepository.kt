package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.dto.ApplicationEntity

interface ApplicationEntityRepository {
    fun findAllByName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ApplicationEntity>

    fun findById(id: Long): ApplicationEntity?

    /**
     * @throws DuplicateKeyException name already exists in namespace
     * @throws DataIntegrityViolationException namespace does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(namespaceEntity: ApplicationEntity): ApplicationEntity

    /**
     * @throws DuplicateKeyException name already exists in namespace
     */
    @Throws(DuplicateKeyException::class)
    fun update(namespaceEntity: ApplicationEntity): ApplicationEntity

    fun removeById(id: Long): Boolean
}
