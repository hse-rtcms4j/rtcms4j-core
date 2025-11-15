package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationSyncStateEntity
import java.time.OffsetDateTime

@Repository
class ConfigurationSyncStateEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigurationSyncStateEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ConfigurationSyncStateEntity> { rs, _ ->
                ConfigurationSyncStateEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configurationId = rs.getLong("configuration_id"),
                    sourceIdentity = rs.getString("source_identity"),
                    commitHash = rs.getString("commit_hash"),
                    isOnline = rs.getBoolean("is_online"),
                )
            }
    }

    /**
     * @throws DataIntegrityViolationException configuration does not exist
     */
    override fun save(configurationSyncStateEntity: ConfigurationSyncStateEntity): ConfigurationSyncStateEntity =
        npJdbc
            .query(
                """
                insert into configuration_sync_state (configuration_id, source_identity, commit_hash, is_online)
                values (:configuration_id, :source_identity, :commit_hash, :is_online)
                returning *;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationSyncStateEntity.configurationId,
                    "source_identity" to configurationSyncStateEntity.sourceIdentity,
                    "commit_hash" to configurationSyncStateEntity.commitHash,
                    "is_online" to configurationSyncStateEntity.isOnline,
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByConfigurationIdAndAfterTimestamp(
        configurationId: Long,
        timestamp: OffsetDateTime,
    ): List<ConfigurationSyncStateEntity> =
        npJdbc
            .query(
                """
                select * from configuration_sync_state
                where configuration_id = :configuration_id and
                      created_at >= :created_at;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                    "created_at" to timestamp,
                ),
                ROW_MAPPER,
            )

    override fun findById(id: Long): ConfigurationSyncStateEntity? =
        npJdbc
            .query(
                """
                select * from configuration_sync_state
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
                delete from configuration_sync_state
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0

    override fun removeAllByConfigurationIdAndBeforeTimestamp(
        configurationId: Long,
        timestamp: OffsetDateTime,
    ): Int =
        npJdbc
            .update(
                """
                delete from configuration_sync_state
                where configuration_id = :configuration_id and
                      created_at < :created_at;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                    "created_at" to timestamp,
                ),
            )
}
