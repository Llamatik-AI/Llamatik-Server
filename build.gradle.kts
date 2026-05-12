plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("io.ktor.plugin") version "3.4.1"
    alias(libs.plugins.shadow)
}

group = "com.llamatik"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.llamatik.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("com.llamatik:library:1.4.0")
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.catching.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.html.builder.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.gson)
    implementation(libs.ktor.server.serialization.kotlinx.json)
    implementation(libs.ktor.server.tomcat)
    implementation(libs.ktor.server.openapi)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.html.jvm)
    implementation(libs.logback.classic)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    implementation(libs.postgresql)
    implementation(libs.hikariCP)
    implementation(libs.swagger.codegen)

    //testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.kotlin.test.junit)
}

tasks.shadowJar {
    setProperty("zip64", true)
}
