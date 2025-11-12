package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class ConfigurationEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigurationEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ConfigurationEntity> { rs, _ ->
                ConfigurationEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
                    applicationId = rs.getLong("application_id"),
                    creatorSub = rs.getObject("creator_sub", UUID::class.java),
                    name = rs.getString("name"),
                    usedCommitHash = rs.getString("used_commit_hash"),
                    streamKey = rs.getString("stream_key"),
                    schemaSourceType = SourceType.valueOf(rs.getString("schema_source_type")),
                )
            }
    }

    /**
     * @throws DuplicateKeyException name duplication
     * @throws DataIntegrityViolationException application does not exist
     */
    override fun save(configurationEntity: ConfigurationEntity): ConfigurationEntity =
        npJdbc
            .query(
                """
                insert into configuration (application_id, creator_sub, name, used_commit_hash, stream_key, schema_source_type)
                values (:application_id, :creator_sub, :name, :used_commit_hash, :stream_key, :schema_source_type)
                returning *;
                """.trimIndent(),
                mapOf(
                    "application_id" to configurationEntity.applicationId,
                    "creator_sub" to configurationEntity.creatorSub,
                    "name" to configurationEntity.name,
                    "used_commit_hash" to configurationEntity.usedCommitHash,
                    "stream_key" to configurationEntity.streamKey,
                    "schema_source_type" to configurationEntity.schemaSourceType.toString(),
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByApplicationId(applicationId: Long): List<ConfigurationEntity> =
        npJdbc
            .query(
                """
                select * from configuration
                where application_id = :application_id;
                """.trimIndent(),
                mapOf(
                    "application_id" to applicationId,
                ),
                ROW_MAPPER,
            )

    override fun findById(id: Long): ConfigurationEntity? =
        npJdbc
            .query(
                """
                select * from configuration
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
                ROW_MAPPER,
            ).firstOrNull()

    override fun removeById(id: Long): Boolean =
        npJdbc
            .update(
                """
                delete from configuration
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
