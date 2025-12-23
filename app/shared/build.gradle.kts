/*
 * Copyright (c) 2025 Lyxot and contributors.
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

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.multiplatform.android)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.build.config)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hot.reload)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

val portable = providers.gradleProperty("portable").map { it.toBoolean() }.getOrElse(false)

kotlin {
    androidLibrary {
        withJava()
        androidResources {
            enable = true
        }
        namespace = "xyz.hyli.timeflow.shared"
        compileSdk = app.versions.compileSdk.get().toInt()
    }

    jvm { }

//    wasmJs {
//        browser()
//        binaries.executable()
//    }


    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":utils"))
            api(project(":app:app-interface"))
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.core)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.composables.core)
            implementation(libs.compose.animation)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.kotlin.inject)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.material.kolor)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jna)
            implementation(libs.jna.platform)
            implementation(libs.kotlinx.coroutines.swing)
        }

        iosMain.dependencies {
            api(project(":app:app-datastore"))
        }

//        wasmJsMain.dependencies {
//
//        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.kotlinx.coroutines.test)
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "App"
                    isStatic = true

                    export(project(":app:app-datastore"))
                }
            }
        }
}

aboutLibraries {
    export {
        prettyPrint = true
        outputFile = file("src/androidMain/res/raw/libraries.json")
    }
    exports {
        create("jvm") {
            prettyPrint = true
            outputFile = file("src/jvmMain/composeResources/files/libraries.json")
        }

        create("ios") {
            prettyPrint = true
            outputFile = file("src/iosMain/composeResources/files/libraries.json")
        }
    }
}

// 自动导出库定义
// Android
tasks.matching {
    it.name == "copyNonXmlValueResourcesForAndroidMain" ||
            it.name.matches(Regex(".*processAndroid.*Resources"))
}.configureEach {
    dependsOn("exportLibraryDefinitions")
}

// Desktop
tasks.matching {
    it.name == "copyNonXmlValueResourcesForJvmMain" ||
            it.name.matches(Regex(".*processJvm.*Resources"))
}.configureEach {
    dependsOn("exportLibraryDefinitionsJvm")
}

// iOS: Run the following command
// ./gradlew :app:exportLibraryDefinitions -PaboutLibraries.outputFile=src/iosMain/composeResources/files/libraries.json -PaboutLibraries.exportVariant=metadataIosMain
val exportLibraryDefinitionsIos by tasks.registering(Exec::class) {
    group = "build"
    description = "Export library definitions for iOS"

    workingDir(rootProject.projectDir)
    commandLine(
        if (System.getProperty("os.name").startsWith("Windows")) "./gradlew.bat" else "./gradlew",
        ":app:shared:exportLibraryDefinitions",
        "-PaboutLibraries.outputFile=src/iosMain/composeResources/files/libraries.json",
        "-PaboutLibraries.exportVariant=metadataIosMain"
    )
}

tasks.matching {
    it.name == "copyNonXmlValueResourcesForIosMain" ||
            it.name.matches(Regex(".*processIos.*Resources"))
}.configureEach {
    dependsOn(exportLibraryDefinitionsIos)
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    packageName = "xyz.hyli.timeflow"
    useKotlinOutput { internalVisibility = false }
    buildConfigField("APP_NAME", "TimeFlow")
    buildConfigField("APP_VERSION_NAME", app.versions.name.get())
    buildConfigField("APP_VERSION_CODE", rootProject.ext.get("appVersionCode").toString().toInt())
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("AUTHOR", "Lyxot")
    buildConfigField("PORTABLE", portable)
}

dependencies {
    with(libs.kotlin.inject.ksp) {
        add("kspAndroid", this)
        add("kspJvm", this)
//        add("kspWasmJs", this)
        add("kspIosX64", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }
}

// 修复 KSP 和 extractAnnotations 任务之间的依赖关系
tasks.matching {
    it.name.contains("extractAndroidMainAnnotations")
}.configureEach {
    dependsOn("kspAndroidMain")
}
