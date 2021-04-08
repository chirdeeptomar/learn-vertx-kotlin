package com.traffix.progress

import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class DatabaseVerticle : CoroutineVerticle() {

    private var client: PgPool? = null

    override fun start(startFuture: Promise<Void>?) {

        val connectOptions = PgConnectOptions()
            .setPort(5432)
            .setHost("localhost")
            .setDatabase("stocks")
            .setUser("postgres")
            .setPassword("postgres")

        // Pool options
        val poolOptions = PoolOptions().setMaxSize(5)

        this.client = PgPool.pool(vertx, connectOptions, poolOptions)

        val consumer = vertx.eventBus().consumer<Stock>("addr.stock.store")

        consumer.handler {
            launch {
                saveToDatabase(it.body())
            }
        }
    }

    private suspend fun saveToDatabase(stock: Stock) {
        coroutineScope {
            val saveResultFuture = client!!
                .preparedQuery("INSERT INTO public.vertx_stock(stock, value) VALUES ($1, $2)")
                .execute(
                    Tuple.of(stock.ticker, stock.value)
                )
            saveResultFuture.onSuccess {
                println("Data Stored Successfully!")
            }
            saveResultFuture.onFailure {
                println("Failure: " + it.cause?.message)
            }
        }
    }
}