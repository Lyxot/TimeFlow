/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.multiplatform.android)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "xyz.hyli.timeflow.client"
        compileSdk = app.versions.compileSdk.get().toInt()
    }
    iosX64 { }
    iosArm64 { }
    iosSimulatorArm64 { }
    jvm { }
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":api:models"))
            implementation(project(":utils"))
            implementation(libs.kermit)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(ktorLibs.client.auth)
            implementation(ktorLibs.client.callId)
            implementation(ktorLibs.client.contentNegotiation)
            implementation(ktorLibs.client.core)
            implementation(ktorLibs.client.encoding)
            implementation(ktorLibs.client.logging)
            implementation(ktorLibs.client.resources)
            implementation(ktorLibs.serialization.kotlinx.json)
            implementation(ktorLibs.serialization.kotlinx.protobuf)
        }

        androidMain.dependencies {
            implementation(ktorLibs.client.okhttp)
        }

        iosMain.dependencies {
            implementation(ktorLibs.client.darwin)
        }

        jvmMain.dependencies {
            implementation(ktorLibs.client.apache5)
        }

        webMain.dependencies {
            implementation(ktorLibs.client.js)
        }
    }
}
