package ru.enzhine.rtcms4j.core.producer

import ru.enzhine.rtcms4j.core.repository.kv.dto.NotificationEvent

interface NotifyEventProducer {
    fun publishEventRetrying(event: NotificationEvent)
}
