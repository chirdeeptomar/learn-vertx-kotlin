package com.traffix.progress.verticles

import com.traffix.progress.EventBusAddress
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory

class JournalVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(JournalVerticle::class.java)

    override fun start(startPromise: Promise<Void>?) {
        val consumer = vertx.eventBus().consumer<String>(EventBusAddress.STOCK_JOURNAL)
        consumer.handler {
            logger.info("Saving to journal: ${it.body()}")
        }
    }
}