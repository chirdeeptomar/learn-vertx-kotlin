import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.osdetector") version "1.7.0"
}

group = "com.traffix"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val vertxVersion = "4.5.7"
val junitJupiterVersion = "5.11.0-M1"
val log4jVersion = "3.0.0-beta2"


val mainVerticleName = "com.traffix.progress.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
    mainClass.set(launcherClassName)
}

dependencies {
    if (osdetector.arch.equals("aarch_64")) {
        implementation("io.netty:netty-all")
    }
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:$log4jVersion"))

    implementation("io.smallrye:jandex:3.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.0")
    implementation("org.jboss.weld.se:weld-se-core:6.0.0.Beta1")
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-infinispan")
    implementation("io.vertx:vertx-web-client")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-pg-client")
    implementation("com.ongres.scram:client:2.1")
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")

    implementation(kotlin("stdlib-jdk8"))
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "21"

kotlin {
    jvmToolchain(21)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)

    // Include resources
    from({
        sourceSets.main.get().resources
    })
}

// Ensure the META-INF/beans.xml file is included in the resources
sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<ShadowJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
    }
}

tasks.withType<JavaExec> {
    args = listOf(
        "run",
        mainVerticleName,
        "--redeploy=$watchForChange",
        "--launcher-class=$launcherClassName",
        "--on-redeploy=$doOnChange"
    )
}
