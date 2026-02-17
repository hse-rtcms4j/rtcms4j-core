package ru.enzhine.rtcms4j.core.producer

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.api.event.NotificationEventDto
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties
import ru.enzhine.rtcms4j.core.mapper.toApi
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotificationEvent

@Component
class NotifyEventProducerImpl(
    private val notifyEventDtoTemplate: RedisTemplate<String, NotificationEventDto>,
    keyValRepositoryProperties: KeyValRepositoryProperties,
) : NotifyEventProducer {
    private val channelName = buildTopicKey(keyValRepositoryProperties)

    override fun publishEvent(event: NotificationEvent) {
        notifyEventDtoTemplate.convertAndSend(channelName, event.toApi())
    }

    private fun buildTopicKey(properties: KeyValRepositoryProperties) = properties.globalPrefix + properties.topic
}
