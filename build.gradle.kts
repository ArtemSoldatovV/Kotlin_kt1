group = "org.example"
version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

val ktor_version = "2.3.2"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")//это для Json()
    implementation("ch.qos.logback:logback-classic:1.4.11")//для slf4j
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.ApplicationKt")
}