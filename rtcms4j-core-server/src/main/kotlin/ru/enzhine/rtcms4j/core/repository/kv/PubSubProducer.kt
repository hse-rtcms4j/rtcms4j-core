package ru.enzhine.rtcms4j.core.repository.kv

import ru.enzhine.rtcms4j.core.repository.kv.dto.NotificationEvent

interface PubSubProducer {
    fun publishEvent(event: NotificationEvent)
}
