package ru.enzhine.rtcms4j.core.repository.db.util

enum class QueryModifier(
    val suffix: String,
) {
    NONE(""),
    FOR_UPDATE("for update"),
    FOR_SHARE("for share"),
}
