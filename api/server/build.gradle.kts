/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

dependencies {
    api(project(":data"))
    implementation(libs.asyncapi.ktor)
    implementation(libs.h2)
    implementation(libs.koog.ktor)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.ktor.server.call.logging)
    implementation(libs.ktor.ktor.server.resources)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.hsts)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.rate.limiting)
    implementation(libs.ktor.server.sessions)
    implementation(libs.logback.classic)
    implementation(libs.postgresql)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.test.host)
}
