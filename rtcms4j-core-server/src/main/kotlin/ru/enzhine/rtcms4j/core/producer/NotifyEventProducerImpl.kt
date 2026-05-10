package ru.enzhine.rtcms4j.core.producer

import io.lettuce.core.RedisConnectionException
import org.slf4j.LoggerFactory
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.api.event.NotificationEventDto
import ru.enzhine.rtcms4j.core.config.props.KeyValRepositoryProperties
import ru.enzhine.rtcms4j.core.mapper.toApi
import ru.enzhine.rtcms4j.core.repository.kv.dto.NotificationEvent
import java.net.ConnectException

@Component
class NotifyEventProducerImpl(
    private val notifyEventDtoTemplate: RedisTemplate<String, NotificationEventDto>,
    keyValRepositoryProperties: KeyValRepositoryProperties,
) : NotifyEventProducer {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val channelName = buildTopicKey(keyValRepositoryProperties)

    @Retryable(
        retryFor = [
            RedisConnectionFailureException::class,
            RedisConnectionException::class,
            ConnectException::class,
            QueryTimeoutException::class,
        ],
        maxAttempts = 3,
        backoff =
            Backoff(
                delay = 1000,
                multiplier = 2.0,
                maxDelay = 5000,
            ),
    )
    override fun publishEventRetrying(event: NotificationEvent) {
        logger.info("Publishing NotifyEvent in topic $channelName.")
        notifyEventDtoTemplate.convertAndSend(channelName, event.toApi())
    }

    private fun buildTopicKey(properties: KeyValRepositoryProperties) = properties.globalPrefix + properties.topic
}
