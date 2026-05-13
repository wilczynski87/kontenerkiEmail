plugins {
    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    application
}

group = "com.kontenery"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.thymeleaf)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.openpdf)
    implementation(libs.flying.saucer.pdf.openpdf)
    implementation(libs.java.mail)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(files("libs/library-1.0.0.jar"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.google.api.services.gmail)
    implementation(libs.google.oauth.client)
    implementation(libs.google.api.client)
    implementation(libs.google.http.client.jackson2)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
}
