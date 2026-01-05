/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

@file:OptIn(
    ExperimentalWasmDsl::class,
    ExperimentalKotlinGradlePluginApi::class,
)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":app:shared"))
    api(project(":app:app-datastore"))
    implementation(compose.desktop.currentOs)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.material3)
    implementation(libs.filekit.core)
    implementation(libs.jbr.api)
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf(
            "-XX:+UseZGC",
//            "-XX:+ZGenerational",
            "-XX:SoftMaxHeapSize=512m",
            "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED"
        )

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )
            packageName = "TimeFlow"
            packageVersion = app.versions.name.get()
            vendor = "Lyxot"
            licenseFile.set(rootProject.rootDir.resolve("LICENSE"))
            modules(
                "jdk.unsupported",
                "java.instrument"
            )

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
                rpmLicenseType = "AGPL-3.0-or-later"
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "ef188802-ed4a-5e96-9bce-e7987aa07e3b"
                shortcut = true
                menu = true
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "xyz.hyli.timeflow"
                appCategory = "public.app-category.productivity"
                jvmArgs += listOf(
                    "-Dapple.awt.application.name=TimeFlow",
                    "-Dsun.java2d.metal=true",
                    "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
                    "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
                )
            }
        }

        buildTypes.release.proguard {
            version.set("7.7.0")
            configurationFiles.from("proguard-rules.pro")
        }
    }
}

//https://github.com/JetBrains/compose-hot-reload
tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("MainKt")
}