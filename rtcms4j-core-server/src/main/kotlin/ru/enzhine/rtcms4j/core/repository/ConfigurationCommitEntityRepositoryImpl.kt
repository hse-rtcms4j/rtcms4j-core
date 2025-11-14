package ru.enzhine.rtcms4j.core.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.enzhine.rtcms4j.core.repository.dto.ConfigurationCommitEntity
import ru.enzhine.rtcms4j.core.repository.dto.SourceType
import java.time.OffsetDateTime

@Repository
class ConfigurationCommitEntityRepositoryImpl(
    private val npJdbc: NamedParameterJdbcTemplate,
) : ConfigurationCommitEntityRepository {
    companion object {
        private val ROW_MAPPER =
            RowMapper<ConfigurationCommitEntity> { rs, _ ->
                ConfigurationCommitEntity(
                    id = rs.getLong("id"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
                    configurationId = rs.getLong("configuration_id"),
                    sourceType = SourceType.valueOf(rs.getString("source_type")),
                    sourceIdentity = rs.getString("source_identity"),
                    commitHash = rs.getString("commit_hash"),
                    jsonValues = rs.getString("json_values"),
                    jsonSchema = rs.getString("json_schema"),
                )
            }
    }

    override fun save(configurationCommitEntity: ConfigurationCommitEntity): ConfigurationCommitEntity {
        TODO("Not yet implemented")
    }

    override fun findAllByConfigurationId(configurationId: Long): List<ConfigurationCommitEntity> {
        TODO("Not yet implemented")
    }

    override fun findAllByConfigurationIdAndCommitHash(
        configurationId: Long,
        commitHash: String,
    ): ConfigurationCommitEntity? {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): ConfigurationCommitEntity? {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long): Boolean {
        TODO("Not yet implemented")
    }
}
