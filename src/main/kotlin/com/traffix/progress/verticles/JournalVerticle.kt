package com.traffix.progress.verticles

import com.traffix.progress.EventBusAddress
import com.traffix.progress.services.AuditService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import jakarta.inject.Inject

class JournalVerticle : AbstractVerticle() {

    @Inject
    private lateinit var auditor: AuditService

    private val logger = LoggerFactory.getLogger(JournalVerticle::class.java)

    override fun start(startPromise: Promise<Void>?) {
        val consumer = vertx.eventBus().consumer<String>(EventBusAddress.STOCK_JOURNAL)
        consumer.handler {
            auditor.audit()
            logger.info("Saving to journal: ${it.body()}")
        }
    }
}