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
    mainClass = "xyz.hyli.timeflow.ApplicationKt"
    applicationDefaultJvmArgs += listOf(
        "-XX:+UseZGC",
//            "-XX:+ZGenerational",
        "-XX:SoftMaxHeapSize=512m",
    )
    applicationName = Target.APP_NAME
}

val bundleWebAppZip = providers.gradleProperty("bundleWebAppZip")
    .map { it.toBoolean() }
    .orElse(false)

ktor {
    fatJar {
        archiveFileName.set(Target.Server.artifactName)
    }
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
    }
}

// Configure Jib platform to match host architecture (applied by Ktor plugin)
val dockerArch: String = providers.gradleProperty("dockerArch")
    .getOrElse(
        System.getProperty("os.arch").let {
            if (it == "aarch64" || it.contains("arm64")) "arm64" else "amd64"
        }
    )

afterEvaluate {
    extensions.configure<com.google.cloud.tools.jib.gradle.JibExtension>("jib") {
        from {
            platforms {
                platform {
                    architecture = dockerArch
                    os = "linux"
                }
            }
        }
    }
}

dependencies {
    api(project(":api:models"))
    implementation(project(":api:ai"))
    implementation(project(":utils"))
    implementation(libs.argon2)
    implementation(libs.asyncapi.ktor)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.jakarta.mail)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)
    implementation(libs.postgresql)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.client.apache5)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(ktorLibs.client.core)
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

sourceSets {
    main {
        resources {
            srcDir(layout.buildDirectory.dir("generated/webAppResources/main"))
            if (!bundleWebAppZip.get()) {
                exclude("static/app.zip")
            }
        }
    }
}

val prepareWebAppResources by tasks.registering(Sync::class) {
    description = "Prepare the optional bundled web app zip for server resources"
    group = "build"

    val generatedResourcesDir = layout.buildDirectory.dir("generated/webAppResources/main/static")
    val jsArtifactDir = rootProject.file("builder/build/artifacts/release/js")
    val wasmJsArtifactDir = rootProject.file("builder/build/artifacts/release/wasmJs")
    val webCompatArtifactDir = rootProject.file("builder/build/artifacts/release/webCompat")

    if (bundleWebAppZip.get() && webCompatArtifactDir.listFiles { file -> file.extension == "zip" }.isNullOrEmpty()) {
        dependsOn(":builder:buildWebCompatReleaseZip")
    }

    into(generatedResourcesDir)
    rename { "app.zip" }
    from(
        provider {
            if (!bundleWebAppZip.get()) {
                emptyList<File>()
            } else {
                val sourceZip = listOf(webCompatArtifactDir, wasmJsArtifactDir, jsArtifactDir)
                    .firstNotNullOfOrNull { dir -> dir.listFiles { file -> file.extension == "zip" }?.firstOrNull() }
                    ?: throw GradleException(
                        "No web app zip file found in ${webCompatArtifactDir.absolutePath}, ${wasmJsArtifactDir.absolutePath}, or ${jsArtifactDir.absolutePath}"
                    )
                listOf(sourceZip)
            }
        }
    )
}

tasks.named("processResources") {
    if (bundleWebAppZip.get()) {
        dependsOn(prepareWebAppResources)
    }
}
