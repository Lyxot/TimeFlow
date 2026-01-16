/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinx.serialization)
    alias(ktorLibs.plugins.ktor)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    applicationDefaultJvmArgs += listOf(
        "-XX:+UseZGC",
//            "-XX:+ZGenerational",
        "-XX:SoftMaxHeapSize=512m",
    )
}

dependencies {
    api(project(":api:models"))
    implementation(project(":utils"))
    implementation(libs.argon2)
    implementation(libs.asyncapi.ktor)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.h2)
    implementation(libs.hikaricp)
    implementation(libs.jakarta.mail)
    implementation(libs.koog.ktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.logback.classic)
    implementation(libs.postgresql)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.serialization.kotlinx.protobuf)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.cachingHeaders)
    implementation(ktorLibs.server.callId)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.conditionalHeaders)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.defaultHeaders)
    implementation(ktorLibs.server.forwardedHeader)
    implementation(ktorLibs.server.hsts)
    implementation(ktorLibs.server.httpRedirect)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.openapi)
    implementation(ktorLibs.server.rateLimit)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.sessions)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(ktorLibs.server.testHost)
    testImplementation(project(":api:client"))
}

val copyWebBuilds by tasks.registering {
    description = "Copy web build zip files to server resources"
    group = "build"

    val resourcesStaticDir = file("src/main/resources/static")
    val jsZipTarget = file("$resourcesStaticDir/app-js.zip")
    val wasmJsZipTarget = file("$resourcesStaticDir/app-wasmJs.zip")
    val jsArtifactDir = rootProject.file("builder/build/artifacts/release/js")
    val wasmJsArtifactDir = rootProject.file("builder/build/artifacts/release/wasmJs")

    outputs.files(jsZipTarget, wasmJsZipTarget)

    // Check if builds are needed and add dependencies
    val jsZipFiles = jsArtifactDir.listFiles { file -> file.extension == "zip" }
    val wasmJsZipFiles = wasmJsArtifactDir.listFiles { file -> file.extension == "zip" }

    if (jsZipFiles.isNullOrEmpty() && !jsZipTarget.exists()) {
        dependsOn(":builder:buildJsReleaseZip")
    }
    if (wasmJsZipFiles.isNullOrEmpty() && !wasmJsZipTarget.exists()) {
        dependsOn(":builder:buildWasmJsReleaseZip")
    }

    doLast {
        // Ensure target directory exists
        resourcesStaticDir.mkdirs()

        // Copy app-js.zip if needed
        if (!jsZipTarget.exists()) {
            val jsFiles = jsArtifactDir.listFiles { file -> file.extension == "zip" }
            if (!jsFiles.isNullOrEmpty()) {
                jsFiles.first().copyTo(jsZipTarget, overwrite = true)
                logger.lifecycle("Copied ${jsFiles.first().name} to app-js.zip")
            } else {
                logger.error("No JS zip file found in ${jsArtifactDir.absolutePath}")
            }
        } else {
            logger.lifecycle("app-js.zip already exists, skipping")
        }

        // Copy app-wasmJs.zip if needed
        if (!wasmJsZipTarget.exists()) {
            val wasmFiles = wasmJsArtifactDir.listFiles { file -> file.extension == "zip" }
            if (!wasmFiles.isNullOrEmpty()) {
                wasmFiles.first().copyTo(wasmJsZipTarget, overwrite = true)
                logger.lifecycle("Copied ${wasmFiles.first().name} to app-wasmJs.zip")
            } else {
                logger.error("No WasmJS zip file found in ${wasmJsArtifactDir.absolutePath}")
            }
        } else {
            logger.lifecycle("app-wasmJs.zip already exists, skipping")
        }
    }
}

tasks.named("processResources") {
    dependsOn(copyWebBuilds)
}
