package ru.enzhine.rtcms4j.core.service.internal.tx

import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

inline fun registerCommitCallback(crossinline block: () -> Unit) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
        block()
        return
    }

    TransactionSynchronizationManager.registerSynchronization(
        object : TransactionSynchronization {
            override fun afterCommit() {
                block()
            }
        },
    )
}
