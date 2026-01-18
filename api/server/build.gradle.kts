/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import xyz.hyli.timeflow.buildsrc.Target

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
    applicationName = Target.APP_NAME
}

ktor {
    fatJar {
        archiveFileName.set(Target.Server.artifactName)
    }

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
    val appZipTarget = file("$resourcesStaticDir/app.zip")
    val jsArtifactDir = rootProject.file("builder/build/artifacts/release/js")
    val wasmJsArtifactDir = rootProject.file("builder/build/artifacts/release/wasmJs")
    val webCompatArtifactDir = rootProject.file("builder/build/artifacts/release/webCompat")

    outputs.files(appZipTarget)

    // Check if builds are needed and add dependencies
    val webCompatZipFiles = webCompatArtifactDir.listFiles { file -> file.extension == "zip" }

    if (webCompatZipFiles.isNullOrEmpty() && !appZipTarget.exists()) {
        dependsOn(":builder:buildWebCompatReleaseZip")
    }

    doLast {
        // Ensure target directory exists
        resourcesStaticDir.mkdirs()

        // Copy app.zip if needed
        if (!appZipTarget.exists()) {
            listOf(webCompatArtifactDir, wasmJsArtifactDir, jsArtifactDir).forEach { dir ->
                val zipFiles = dir.listFiles { file -> file.extension == "zip" }
                if (!zipFiles.isNullOrEmpty()) {
                    zipFiles.first().copyTo(appZipTarget, overwrite = true)
                    logger.lifecycle("Copied ${zipFiles.first().name} to app.zip")
                    return@doLast
                }
            }
            logger.error("No zip file found in ${webCompatArtifactDir.absolutePath} or ${jsArtifactDir.absolutePath} or ${wasmJsArtifactDir.absolutePath}")
        } else {
            logger.lifecycle("app.zip already exists, skipping")
        }
    }
}

tasks.named("processResources") {
    dependsOn(copyWebBuilds)
}
