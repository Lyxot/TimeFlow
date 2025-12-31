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
    alias(libs.plugins.build.config)
}

kotlin {
    androidLibrary {
        withJava()
        namespace = "xyz.hyli.timeflow.utils"
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
            implementation(libs.korlibs.compression)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

val portable: Boolean = providers.gradleProperty("portable").map { it.toBoolean() }.getOrElse(false)

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    packageName = "xyz.hyli.timeflow"
    useKotlinOutput { internalVisibility = false }
    buildConfigField("APP_NAME", "TimeFlow")
    buildConfigField("APP_VERSION_NAME", app.versions.name.get())
    buildConfigField("APP_VERSION_CODE", rootProject.ext.get("appVersionCode") as Int)
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("GIT_COMMIT_HASH", rootProject.ext.get("commitHash") as String)
    buildConfigField("AUTHOR", "Lyxot")
    buildConfigField("PORTABLE", portable)
}