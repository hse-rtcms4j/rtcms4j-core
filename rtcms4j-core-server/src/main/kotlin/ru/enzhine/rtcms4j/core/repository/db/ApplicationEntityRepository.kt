package ru.enzhine.rtcms4j.core.repository.db

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.enzhine.rtcms4j.core.repository.db.dto.ApplicationEntity
import ru.enzhine.rtcms4j.core.repository.db.util.QueryModifier

interface ApplicationEntityRepository {
    fun findAllByName(
        namespaceId: Long,
        name: String?,
        pageable: Pageable,
    ): Page<ApplicationEntity>

    fun findById(
        id: Long,
        modifier: QueryModifier = QueryModifier.NONE,
    ): ApplicationEntity?

    /**
     * @throws DuplicateKeyException name already exists in namespace
     * @throws DataIntegrityViolationException namespace does not exist
     */
    @Throws(DuplicateKeyException::class, DataIntegrityViolationException::class)
    fun save(applicationEntity: ApplicationEntity): ApplicationEntity

    /**
     * @throws DuplicateKeyException name already exists in namespace
     */
    @Throws(DuplicateKeyException::class)
    fun update(applicationEntity: ApplicationEntity): ApplicationEntity

    fun removeById(id: Long): Boolean
}
