@file:OptIn(
    ExperimentalWasmDsl::class,
    ExperimentalKotlinGradlePluginApi::class,
    org.jetbrains.compose.ExperimentalComposeLibrary::class
)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.lang.System.getenv
import java.net.HttpURLConnection
import java.net.URL

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.build.config)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hot.reload)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

val appVersionCode = app.versions.major.get().toInt() * 10000 +
        try {
            val url = URL("https://api.github.com/repos/Lyxot/TimeFlow/commits?sha=master&per_page=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            val linkHeader = connection.getHeaderField("Link") ?: ""
            val lastPagePattern = ".*page=(\\d+)>; rel=\"last\".*".toRegex()
            val match = lastPagePattern.find(linkHeader)
            match?.groupValues?.get(1)?.toInt() ?: 0
        } catch (e: Exception) {
            println("Error getting commit count from GitHub API: ${e.message}")
            0
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
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            // https://github.com/JetBrains/compose-multiplatform/commit/2dfb657dec3eb5e00cf64a0c8cd283bc4ba78ab7
            implementation(libs.material3)
            implementation(libs.material3.adaptive.navigation.suite)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(libs.adaptive)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.coil)
            implementation(libs.composables.core)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.dnd)
            implementation(libs.kermit)
            implementation(libs.kotlin.inject)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.material.kolor)
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

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(compose.ui)
            implementation(compose.uiTest)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }

    sourceSets.all {
        languageSettings.apply {
            languageVersion = "2.2"
            apiVersion = "2.2"
            progressiveMode = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "xyz.hyli.timeflow"
    compileSdk = app.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = app.versions.minSdk.get().toInt()
        targetSdk = app.versions.targetSdk.get().toInt()

        applicationId = "xyz.hyli.timeflow"
        versionCode = appVersionCode
        versionName = app.versions.name.get()
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
        isCoreLibraryDesugaringEnabled = true
    }
    dependencies {
        coreLibraryDesugaring(libs.desugar.jdk.libs)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TimeFlow"
            packageVersion = app.versions.name.get()
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
    buildConfigField("APP_VERSION_NAME", app.versions.name.get())
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
