package com.traffix.progress

import com.traffix.progress.verticles.DatabaseVerticle
import com.traffix.progress.verticles.StockPriceFinderVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
import io.vertx.core.ThreadingModel
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.*

data class Stock(val ticker: String, val value: Float)

class MainVerticle : CoroutineVerticle(), CoroutineRouterSupport, CoroutineEventBusSupport {

    private val logger = LoggerFactory.getLogger(MainVerticle::class.java)

    private val options: DeploymentOptions = DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)

    override fun start(startFuture: Promise<Void>?) {
        vertx.eventBus().registerDefaultCodec(
            Stock::class.java,
            GenericCodec(Stock::class.java)
        )

        vertx.deployVerticle(StockPriceFinderVerticle(), options)
        vertx.deployVerticle(DatabaseVerticle(), options)

        val router: Router = Router.router(vertx)

        coroutineRouter {
            router.get("/api/").coHandler { extracted(it) }
            router.get("/api/stocks/:name").coHandler { getStockPrice(it) }
        }

        val server = vertx.createHttpServer()
        val port = Integer.parseInt(System.getProperty("http.port", "8888"))
        server.requestHandler(router).listen(port) { http ->
            if (http.succeeded()) {
                logger.info("HTTP server started on port $port")
                startFuture?.complete()
            } else {
                startFuture?.fail(http.cause());
            }
        }
    }

    private fun extracted(it: RoutingContext) {
        it.response().end("STOCKS API")
    }

    private suspend fun getStockPrice(ctx: RoutingContext) {
        val stock = ctx.pathParam("name")
        // Send a message and wait for a reply
        val reply = awaitResult<Message<Float>> {
            vertx.eventBus().request("addr.stock.name", stock, it)
        }

        val price = reply.body()
        logger.info("Successfully fetched price for ${stock}: $price")
        vertx.eventBus().send("addr.stock.store", Stock(stock, price))
        ctx.response().end(JsonObject().put(stock, Json.encode(price)).encode())
    }
}
