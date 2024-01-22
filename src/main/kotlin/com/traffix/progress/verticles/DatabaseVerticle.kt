package com.traffix.progress.verticles

import com.traffix.progress.MainVerticle
import com.traffix.progress.Stock
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineEventBusSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

class DatabaseVerticle : CoroutineVerticle(), CoroutineEventBusSupport {

    private val logger = LoggerFactory.getLogger(MainVerticle::class.java)

    private var client: SqlClient? = null

    override suspend fun start() {
        val connectOptions = PgConnectOptions()
            .setPort(5432)
            .setHost("localhost")
            .setDatabase("vertx_stocks")
            .setUser("postgres")
            .setPassword("postgres")

        // Pool options
        val poolOptions = PoolOptions().setMaxSize(5)

        this.client = PgBuilder
            .client()
            .with(poolOptions)
            .connectingTo(connectOptions)
            .using(vertx)
            .build()

        val consumer = vertx.eventBus().consumer<Stock>("addr.stock.store")

        consumer.coHandler {
            logger.info("From Eventbus Consumer:: DatabaseVerticle:: Running inside Thread: ${Thread.currentThread().id}")

            logger.info("Payload Received: ${it.body()}")

            saveToDatabase(it.body())

        }.completionHandler {
            if (it.failed())
                logger.error(it.cause())
        }
    }

    private fun saveToDatabase(stock: Stock) {
        logger.info("Storing stock price in the database: $stock")

        val saveResultFuture = client!!
            .preparedQuery("INSERT INTO public.stocks(stock, price) VALUES ($1, $2)")
            .execute(
                Tuple.of(stock.ticker, stock.value)
            )
        saveResultFuture.onSuccess {
            println("Data Stored Successfully!")
        }
        saveResultFuture.onFailure {
            println("Failure: " + it.cause)
            throw it
        }
    }
}
