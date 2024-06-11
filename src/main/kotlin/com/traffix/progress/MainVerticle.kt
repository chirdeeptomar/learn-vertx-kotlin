package com.traffix.progress

import com.traffix.progress.services.Auditor
import com.traffix.progress.verticles.DatabaseVerticle
import com.traffix.progress.verticles.JournalVerticle
import com.traffix.progress.verticles.StockPriceFinderVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.*
import org.jboss.weld.environment.se.Weld
import java.io.Serializable


object EventBusAddress {
    const val STOCK_NAME = "addr.stock.name"
    const val STOCK_STORE = "addr.stock.store"
    const val STOCK_JOURNAL = "addr.stock.journal"
}

data class Stock(val ticker: String, val value: Float) : Serializable

class MainVerticle : CoroutineVerticle(), CoroutineRouterSupport, CoroutineEventBusSupport {

    // Instantiate the container
    private val container = Weld()
        .addBeanClasses(Auditor::class.java)
        .addBeanClasses(DatabaseVerticle::class.java)
        .addBeanClasses(StockPriceFinderVerticle::class.java)
        .addBeanClasses(JournalVerticle::class.java)
        .disableDiscovery()
        .initialize()

    private val logger = LoggerFactory.getLogger(MainVerticle::class.java)

    private val options: DeploymentOptions = DeploymentOptions().setInstances(2)

    override fun start(startFuture: Promise<Void>?) {
        logger.info("Deploying Main verticle in ${if (vertx.isClustered) "Clustered" else "Non-Clustered"} mode.")

        logger.info("Registering Codecs")
        vertx.eventBus().registerDefaultCodec(
            Stock::class.java, GenericCodec(Stock::class.java)
        )

        vertx.deployVerticle({ container.select(StockPriceFinderVerticle::class.java).get() }, options)
        vertx.deployVerticle({ container.select(DatabaseVerticle::class.java).get() }, options)
        vertx.deployVerticle({ container.select(JournalVerticle::class.java).get() }, options)

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
                startFuture?.fail(http.cause())
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
            vertx.eventBus().request(EventBusAddress.STOCK_NAME, stock, it)
        }

        val price = reply.body()
        logger.info("Successfully fetched price for ${stock}: $price")
        vertx.eventBus().send(EventBusAddress.STOCK_JOURNAL, "$stock:$price")
        vertx.eventBus().send(EventBusAddress.STOCK_STORE, Stock(stock, price))
        ctx.response().end(JsonObject().put(stock, Json.encode(price)).encode())
    }
}


fun main() {
    val mgr: ClusterManager = InfinispanClusterManager()

    Vertx.builder().withClusterManager(mgr).buildClustered().onComplete { res ->
        if (res.succeeded()) {
            val vertx: Vertx = res.result()
            vertx.eventBus().registerDefaultCodec(
                Stock::class.java, GenericCodec(Stock::class.java)
            )
            vertx.deployVerticle(MainVerticle::class.java.getName())
        } else {
            // failed!
            System.err.println("Cannot create vert.x instance : " + res.cause())
        }
    }
}
