package com.traffix.progress.verticles

import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineEventBusSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ThreadLocalRandom

class StockPriceFinderVerticle : CoroutineVerticle(), CoroutineEventBusSupport {
    private val logger = LoggerFactory.getLogger(StockPriceFinderVerticle::class.java)

    override suspend fun start() {
        val consumer = vertx.eventBus().consumer<String>("addr.stock.name")

        consumer.coHandler {
            logger.warn("From Eventbus Consumer::StockPriceFinderVerticle :: Running inside Thread: ${Thread.currentThread().id}")

            launch {
                logger.debug("From Eventbus Launch::StockPriceFinderVerticle :: Running inside Thread: ${Thread.currentThread().id}")

                val result = getStockPrice(it.body())
                it.reply(result)
            }
        }.completionHandler {
            if (it.failed())
                logger.error(it.cause())
        }
    }

    private suspend fun getStockPrice(stock: String): Float {
        return withContext(Dispatchers.IO) {
            logger.info("Getting price for $stock:: Running inside Thread: ${Thread.currentThread().id}")
            return@withContext ThreadLocalRandom.current().nextFloat()
        }
    }
}
