package ru.enzhine.rtcms4j.core.service.internal.dto

data class AvailableResources(
    val namespaces: List<Namespace>,
    val applications: List<Application>,
)
