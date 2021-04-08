package com.traffix.progress

import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class StockPriceFinderVerticle : CoroutineVerticle() {

    val verticleId: UUID = UUID.randomUUID()

    override fun start(startFuture: Promise<Void>?) {
        val consumer = vertx.eventBus().consumer<String>("addr.stock.name")

        consumer.handler { it ->
            println("From Eventbus Consumer:: Running inside Thread: ${Thread.currentThread().id}")

            launch {
                println("From Eventbus Launch:: Running inside Thread: ${Thread.currentThread().id}")
                val result = getStockPrice(it.body())
                it.reply(result)
            }
        }
    }

    private suspend fun getStockPrice(stock: String): Float {
        return withContext(Dispatchers.IO) {
            println("From getStockPrice:: Running inside Thread: ${Thread.currentThread().id}")
            return@withContext Float.MAX_VALUE
        }
    }
}