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
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget { }
    iosX64 { }
    iosArm64 { }
    iosSimulatorArm64 { }
    jvm { }
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

android {
    namespace = "xyz.hyli.timeflow.utils"
    compileSdk = app.versions.compileSdk.get().toInt()
}
