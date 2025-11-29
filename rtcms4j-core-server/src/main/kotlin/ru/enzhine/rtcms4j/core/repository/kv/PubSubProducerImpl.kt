package ru.enzhine.rtcms4j.core.repository.kv

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotifyEventDto

@Component
class PubSubProducerImpl(
    private val notifyEventDtoTemplate: RedisTemplate<String, NotifyEventDto>,
    keyValRepositoryProperties: KeyValRepositoryProperties,
) : PubSubProducer {
    private val channelTopic = buildTopicKey(keyValRepositoryProperties)

    private fun buildTopicKey(properties: KeyValRepositoryProperties) = properties.globalPrefix + properties.topic

    override fun publishEvent(event: NotifyEventDto) {
        notifyEventDtoTemplate.convertAndSend(channelTopic, event)
    }
}
