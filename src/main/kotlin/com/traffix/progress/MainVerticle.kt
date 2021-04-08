package com.traffix.progress

import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch

data class Stock(val ticker: String, val value: Float)

class MainVerticle : CoroutineVerticle() {

    private val options: DeploymentOptions = DeploymentOptions().setWorker(true)

    override fun start(startFuture: Promise<Void>?) {
        vertx.eventBus().registerDefaultCodec(
            Stock::class.java,
            GenericCodec(Stock::class.java)
        )

        vertx.deployVerticle(StockPriceFinderVerticle(), options)
        vertx.deployVerticle(DatabaseVerticle(), options)

        val router: Router = Router.router(vertx)
        router.get("/api/").coroutineHandler {
            it.response().end("STOCKS API")
        }

        router.get("/api/stocks/").coroutineHandler(this::getStockPrice)

        val server = vertx.createHttpServer()
        val port = Integer.parseInt(System.getenv().getOrDefault("http.port", "8888"))
        println("Port to run on: ${System.getenv()["http.port"]}")
        server.requestHandler(router).listen(port) { http ->
            if (http.succeeded()) {
                startFuture?.complete()
                println("HTTP server started on port $port")
            } else {
                startFuture?.fail(http.cause());
            }
        }
    }

    private fun getStockPrice(ctx: RoutingContext) {
        val stock = ctx.queryParam("stock")[0]
        // Send a message and wait for a reply
        vertx.eventBus().request<Float>("addr.stock.name", stock) {
            vertx.eventBus().send("addr.stock.store", Stock(stock, it.result().body()))
            ctx.response().end("$stock valued at: ${Json.encode(it.result().body())}")
        }
    }

    /**
     * An extension method for simplifying coroutines usage with Vert.x Web routers
     */
    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }
}
