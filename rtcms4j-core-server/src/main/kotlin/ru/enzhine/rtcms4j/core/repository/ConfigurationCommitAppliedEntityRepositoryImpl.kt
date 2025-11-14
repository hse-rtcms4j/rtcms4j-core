package ru.enzhine.rtcms4j.core.repository

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitAppliedEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import java.time.OffsetDateTime

@Repository
class ConfigurationCommitAppliedEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigurationCommitAppliedEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ConfigurationCommitAppliedEntity> { rs, _ ->
                ConfigurationCommitAppliedEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                    commitHash = rs.getString("commit_hash"),
                    configurationCommitId = rs.getLong("configuration_commit_id"),
                )
            }
    }

    /**
     * @throws DataIntegrityViolationException configuration does not exist
     */
    override fun save(configurationCommitAppliedEntity: ConfigurationCommitAppliedEntity): ConfigurationCommitAppliedEntity =
        npJdbc
            .query(
                """
                insert into configuration_commit_applied (configuration_id, source_type, source_identity, commit_hash, configuration_commit_id)
                values (:configuration_id, :source_type, :source_identity, :commit_hash, :configuration_commit_id)
                returning *;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationCommitAppliedEntity.configurationId,
                    "source_type" to configurationCommitAppliedEntity.sourceType,
                    "source_identity" to configurationCommitAppliedEntity.sourceIdentity,
                    "commit_hash" to configurationCommitAppliedEntity.commitHash,
                    "configuration_commit_id" to configurationCommitAppliedEntity.configurationCommitId,
                ),
                ROW_MAPPER,
            ).first()

    override fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitAppliedEntity> =
        npJdbc
            .query(
                """
                select * from configuration_commit_applied
                where configuration_id = :configuration_id;
                """.trimIndent(),
                mapOf(
                    "configuration_id" to configurationId,
                ),
                ROW_MAPPER,
            )

    override fun findById(id: Long): ConfigurationCommitAppliedEntity? =
        npJdbc
            .query(
                """
                select * from configuration_commit_applied
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
                delete from configuration_commit_applied
                where id = :id;
                """.trimIndent(),
                mapOf(
                    "id" to id,
                ),
            ) != 0
}
