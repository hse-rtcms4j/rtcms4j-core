package ru.enzhine.rtcms4j.core.repository.kv

import ru.enzhine.rtcms4j.core.repository.kv.dto.NotifyEventDto

interface PubSubProducer {
    fun publishEvent(event: NotifyEventDto)
}
