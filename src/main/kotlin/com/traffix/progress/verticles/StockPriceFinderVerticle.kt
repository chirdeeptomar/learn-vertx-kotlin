package com.traffix.progress.verticles

import com.traffix.progress.EventBusAddress
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
        val consumer = vertx.eventBus().consumer<String>(EventBusAddress.STOCK_NAME)

        consumer.coHandler {
            logger.debug("From Eventbus Consumer::StockPriceFinderVerticle :: Running inside Thread: ${Thread.currentThread().threadId()}")

            launch {
                logger.debug("From Eventbus Launch::StockPriceFinderVerticle :: Running inside Thread: ${Thread.currentThread().threadId()}")

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
            logger.debug("Getting price for $stock:: Running inside Thread: ${Thread.currentThread().threadId()}")
            return@withContext ThreadLocalRandom.current().nextFloat()
        }
    }
}
