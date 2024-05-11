# Vertx with Kotlin & Postgres

Learning Vertx with Kotlin and Coroutine

Create the database, using postgres for database server:

```sql
create database vertx_stocks;
create table stocks("stock" varchar, "price" float);
```

Build:

```shell
gradlew.bat clean build
```

#### Run (without clustering):
```bash
java -jar build\libs\learn-vertx-kotlin-1.0.0-SNAPSHOT.jar
```

#### Run (with clustering):
Using Infinispan for clustering.

```bash
java -Djava.net.preferIPv4Stack=true -Dhttp.port=9001 -jar build/libs/learn-vertx-kotlin-1.0.0-SNAPSHOT.jar -cluster
java -Djava.net.preferIPv4Stack=true -Dhttp.port=9002 -jar build/libs/learn-vertx-kotlin-1.0.0-SNAPSHOT.jar -cluster
```
Sample request:
```
http://localhost:9001/api/stocks/msft
```

```shell
for run in {1..100}; do curl "http://localhost:9002/api/stocks/msft" &; done                                                ─╯
```
