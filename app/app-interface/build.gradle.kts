/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

@file:OptIn(ExperimentalWasmDsl::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.multiplatform.android)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidLibrary {
        withJava()
        namespace = "xyz.hyli.timeflow.interface"
        compileSdk = app.versions.compileSdk.get().toInt()
    }
    iosX64 { }
    iosArm64 { }
    iosSimulatorArm64 { }
    jvm { }
    js {
        browser()
        binaries.executable()
    }
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":data"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.korlibs.compression)
            implementation(libs.kotlinx.serialization.protobuf)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }

        iosMain.dependencies {

        }
    }
}
