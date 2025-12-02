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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.lang.System.getenv

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.build.config)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hot.reload)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

// Value source to get commit count from git (cacheable by Gradle configuration cache)
abstract class GitCommitCountValueSource : ValueSource<Int, GitCommitCountValueSource.Params> {
    interface Params : ValueSourceParameters {
        val workingDir: Property<String>
    }

    override fun obtain(): Int {
        return try {
            val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
                .directory(File(parameters.workingDir.get()))
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                result.toIntOrNull() ?: 0
            } else {
                throw RuntimeException("git command failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            // Fallback to GitHub API
            try {
                val url = java.net.URL("https://api.github.com/repos/Lyxot/TimeFlow/commits?sha=master&per_page=1")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                val linkHeader = connection.getHeaderField("Link") ?: ""
                val lastPagePattern = ".*page=(\\d+)>; rel=\"last\".*".toRegex()
                val match = lastPagePattern.find(linkHeader)
                match?.groupValues?.get(1)?.toInt() ?: 0
            } catch (apiError: Exception) {
                0
            }
        }
    }
}

val commitCount: Provider<Int> = providers.of(GitCommitCountValueSource::class) {
    parameters {
        workingDir.set(rootProject.projectDir.absolutePath)
    }
}

val appVersionCode = app.versions.major.get().toInt() * 10000 + commitCount.get()

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
            implementation(libs.androidx.datastore)
            implementation(libs.kotlin.inject)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.protobuf)
            implementation(libs.material.kolor)
        }

        androidMain.dependencies {
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
            implementation(libs.compose.ui.test)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.kotlinx.coroutines.test)
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
    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "TimeFlow-$versionName.apk"
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
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("MainKt")
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
tasks.named("preBuild") {
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
// ./gradlew :composeApp:exportLibraryDefinitions -PaboutLibraries.outputFile=src/iosMain/composeResources/files/libraries.json -PaboutLibraries.exportVariant=metadataIosMain
val exportLibraryDefinitionsIos by tasks.registering(Exec::class) {
    group = "build"
    description = "Export library definitions for iOS"

    workingDir(rootProject.projectDir)
    commandLine(
        "./gradlew",
        ":composeApp:exportLibraryDefinitions",
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

fun ipaArguments(
    destination: String = "generic/platform=iOS",
    sdk: String = "iphoneos",
): Array<String> {
    return arrayOf(
        "xcodebuild",
        "-project", rootDir.resolve("iosApp/iosApp.xcodeproj").absolutePath,
        "-scheme", "iosApp",
        "-destination", destination,
        "-sdk", sdk,
        "CODE_SIGNING_ALLOWED=NO",
        "CODE_SIGNING_REQUIRED=NO",
    )
}


val buildDebugArchive = tasks.register("buildDebugArchive", Exec::class) {
    group = "build"
    description = "Builds the iOS framework for Debug"
    workingDir(projectDir)

    val output = layout.buildDirectory.dir("archives/debug/TimeFlow.xcarchive")
    outputs.dir(output)
    commandLine(
        *ipaArguments(),
        "archive",
        "-configuration", "Debug",
        "-archivePath", output.get().asFile.absolutePath,
    )
}

val buildReleaseArchive = tasks.register("buildReleaseArchive", Exec::class) {
    group = "build"
    description = "Builds the iOS framework for Release"
    workingDir(projectDir)

    val output = layout.buildDirectory.dir("archives/release/TimeFlow.xcarchive")
    outputs.dir(output)
    commandLine(
        *ipaArguments(),
        "archive",
        "-configuration", "Release",
        "-archivePath", output.get().asFile.absolutePath,
    )
}

/**
 * Packages an unsigned IPA **and** injects an ad‑hoc signature so sideloaders can re‑sign it.
 *
 * This task is **configuration‑cache safe** – it does *not* capture the `Project` instance.
 */
/**
 * Packages an unsigned IPA **and** injects an ad‑hoc signature so sideloaders can re‑sign it.
 *
 * This task is **configuration‑cache safe** – it does *not* capture the `Project` instance.
 */
@CacheableTask
abstract class BuildIpaTask : DefaultTask() {

    /* -------------------------------------------------------------
     * Inputs / outputs
     * ----------------------------------------------------------- */

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val archiveDir: DirectoryProperty

    @get:OutputFile
    abstract val outputIpa: RegularFileProperty

    /* -------------------------------------------------------------
     * Services (injected)
     * ----------------------------------------------------------- */

    @get:Inject
    abstract val execOperations: ExecOperations

    /* -------------------------------------------------------------
     * Action
     * ----------------------------------------------------------- */

    @TaskAction
    fun buildIpa() {
        // 1. Locate the .app inside the .xcarchive
        val appDir = archiveDir.get().asFile.resolve("Products/Applications/TimeFlow.app")
        if (!appDir.exists())
            throw GradleException("Could not find TimeFlow.app in archive at: ${appDir.absolutePath}")

        // 2. Create temporary Payload directory and copy .app into it
        val payloadDir = File(temporaryDir, "Payload").apply { mkdirs() }
        val destApp = File(payloadDir, appDir.name)
        appDir.copyRecursively(destApp, overwrite = true)

        // 3. Inject placeholder (ad‑hoc) code signature so AltStore / SideStore accept it
        logger.lifecycle("[IPA] Ad‑hoc signing ${destApp.name} …")
        execOperations.exec {
            commandLine(
                "codesign", "--force", "--deep", "--sign", "-", "--timestamp=none",
                destApp.absolutePath,
            )
        }

        // 4. Zip Payload ⇒ .ipa using the system `zip` command
        //
        //    -r : recurse into directories
        //    -y : store symbolic links as the link instead of the referenced file
        //
        // The working directory is the temporary folder so the archive
        // has a top‑level "Payload/" directory (required for .ipa files).
        val zipFile = File(temporaryDir, "TimeFlow.zip")
        execOperations.exec {
            workingDir(temporaryDir)
            commandLine("zip", "-r", "-y", zipFile.absolutePath, "Payload")
        }

        // 5. Move to final location (with .ipa extension)
        outputIpa.get().asFile.apply {
            parentFile.mkdirs()
            delete()
            zipFile.renameTo(this)
        }

        logger.lifecycle("[IPA] Created ad‑hoc‑signed IPA at: ${outputIpa.get().asFile.absolutePath}")
    }
}

tasks.register("buildDebugIpa", BuildIpaTask::class) {
    description = "Manually packages the .app from the .xcarchive into an unsigned .ipa"
    group = "build"

    // Adjust these paths as needed
    archiveDir = layout.buildDirectory.dir("archives/debug/TimeFlow.xcarchive")
    outputIpa = layout.buildDirectory.file("archives/debug/TimeFlow-${app.versions.name.get()}.ipa")
    dependsOn(buildDebugArchive)
}

tasks.register("buildReleaseIpa", BuildIpaTask::class) {
    description = "Manually packages the .app from the .xcarchive into an unsigned .ipa"
    group = "build"

    // Adjust these paths as needed
    archiveDir = layout.buildDirectory.dir("archives/release/TimeFlow.xcarchive")
    outputIpa =
        layout.buildDirectory.file("archives/release/TimeFlow-${app.versions.name.get()}.ipa")
    dependsOn(buildReleaseArchive)
}
