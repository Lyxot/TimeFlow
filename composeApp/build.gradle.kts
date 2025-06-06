@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.ComposeHotRun
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.lang.System.getenv

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.build.config)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hot.reload)
    alias(libs.plugins.jgit)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

val appMajorVersionCode = libs.versions.app.version.major.get().toInt()
val appVersionCode = appMajorVersionCode * 10000 +
        if (getenv("CI") == "true") {
            getenv("COMMIT_COUNT").toInt()
        } else {
            jgit.repo()?.commitCount("refs/remotes/origin/${jgit.repo()?.raw?.branch ?: "master"}") ?: 0
        }

kotlin {
    androidTarget {
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

//    wasmJs {
//        browser()
//        binaries.executable()
//    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            export(libs.calf.ui)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.calf.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.coil)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.dnd)
            implementation(libs.kermit)
            implementation(libs.kotlin.inject)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.ktorfit.lib)
            implementation(libs.material.kolor)
            implementation(libs.material3.window.size)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.startup.runtime)
            implementation(libs.kotlinx.coroutines.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.appdirs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        iosMain.dependencies {

        }

//        wasmJsMain.dependencies {
//
//        }
    }

    sourceSets.all {
        languageSettings.apply {
            languageVersion = "2.1"
            apiVersion = "2.1"
            progressiveMode = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "xyz.hyli.timeflow"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35

        applicationId = "xyz.hyli.timeflow"
        versionCode = appVersionCode
        versionName = libs.versions.app.version.name.get()
    }
    signingConfigs {
        create("release") {
            storeFile = file("release-key.jks")
            storePassword = getenv("RELEASE_KEY_STORE_PASSWORD")
            keyAlias = "TimeFlow"
            keyPassword = getenv("RELEASE_KEY_PASSWORD")
            enableV1Signing = false
            enableV2Signing = true
            enableV3Signing = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        if (getenv("RELEASE_KEY_EXISTS") == "true") {
            getByName("release") {
                isShrinkResources = true
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TimeFlow"
            packageVersion = libs.versions.app.version.name.get()
            modules(
                "jdk.unsupported",
                "java.instrument"
            )

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "ef188802-ed4a-5e96-9bce-e7987aa07e3b"
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "xyz.hyli.timeflow"
                jvmArgs += listOf("-Dapple.awt.application.name=TimeFlow")
            }
        }

        buildTypes.release.proguard {
            version.set("7.7.0")
            configurationFiles.from("proguard-rules.pro")
        }
    }
}

//https://github.com/JetBrains/compose-hot-reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("MainKt")
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    packageName = "xyz.hyli.timeflow"
    useKotlinOutput()
    buildConfigField("APP_NAME", "TimeFlow")
    buildConfigField("APP_VERSION_NAME", libs.versions.app.version.name)
    buildConfigField("APP_VERSION_CODE", appVersionCode)
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("AUTHOR", "Lyxot")
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
