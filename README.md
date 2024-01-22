# learn-vertx-kotlin
Learning Vertx with Kotlin and Coroutine

Create the database:
- create database vertx_stocks;
- create table stocks("stock" varchar, "price" float);

Build: gradlew.bat clean build

Run (without clustering): java -jar build\libs\progress-management-1.0.0-SNAPSHOT-fat.jar

Run (with clustering):

- java -Djava.net.preferIPv4Stack=true -Dhttp.port=9070 -jar build/libs/progress-management-1.0.0-SNAPSHOT-fat.jar -cluster
- java -Djava.net.preferIPv4Stack=true -Dhttp.port=9080 -jar build/libs/progress-management-1.0.0-SNAPSHOT-fat.jar -cluster

