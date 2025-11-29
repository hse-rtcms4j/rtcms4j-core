package ru.enzhine.rtcms4j.core.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotifyEventDto

@Component
class NotifyEventDtoSerializer(
    private val objectMapper: ObjectMapper,
) : RedisSerializer<NotifyEventDto> {
    override fun serialize(value: NotifyEventDto?): ByteArray? = value?.let { it -> objectMapper.writeValueAsBytes(it) }

    override fun deserialize(bytes: ByteArray?): NotifyEventDto? =
        bytes?.let { it -> objectMapper.readValue(it, NotifyEventDto::class.java) }
}
