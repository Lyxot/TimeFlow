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

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
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
            api(project(":app:shared"))
            implementation(project(":app:app-localstorage"))
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.material3)
        }
    }
}

/**
 * Injects <link rel="preload"> tags into index.html by scanning the distribution
 * directory for large/important assets (.wasm, .mjs, fonts, large .js bundles).
 */
abstract class InjectPreloadTask : DefaultTask() {
    @get:InputDirectory
    abstract val distDir: DirectoryProperty

    @TaskAction
    fun inject() {
        val dir = distDir.get().asFile
        val indexFile = dir.resolve("index.html")
        if (!indexFile.exists()) return

        val html = indexFile.readText()
        val preloadEntries = mutableListOf<String>()

        dir.walkTopDown().filter { it.isFile }.forEach { file ->
            val relativePath = "./${file.relativeTo(dir).path}"
            val sizeKB = file.length() / 1024

            // Skip files already preloaded in the source HTML
            if (html.contains(relativePath)) return@forEach

            when {
                file.extension == "wasm" ->
                    preloadEntries += """        <link rel="preload" href="$relativePath" as="fetch" type="application/wasm" crossorigin/>"""
                file.extension == "mjs" ->
                    preloadEntries += """        <link rel="modulepreload" href="$relativePath"/>"""
                file.extension == "ttf" ->
                    preloadEntries += """        <link rel="preload" href="$relativePath" as="fetch" type="font/ttf" crossorigin/>"""
                file.extension == "woff2" ->
                    preloadEntries += """        <link rel="preload" href="$relativePath" as="fetch" type="font/woff2" crossorigin/>"""
                file.extension == "js" && sizeKB > 50 && !file.name.endsWith(".map") ->
                    preloadEntries += """        <link rel="preload" href="$relativePath" as="script"/>"""
            }
        }

        if (preloadEntries.isEmpty()) return

        val injection = preloadEntries.joinToString("\n") + "\n"
        indexFile.writeText(html.replace("</head>", "$injection    </head>"))
        logger.lifecycle("Injected ${preloadEntries.size} preload tags into ${indexFile.path}")
    }
}

mapOf(
    "jsBrowserDistribution" to "dist/js/productionExecutable",
    "wasmJsBrowserDistribution" to "dist/wasmJs/productionExecutable",
    "composeCompatibilityBrowserDistribution" to "dist/composeWebCompatibility/productionExecutable"
).forEach { (distTaskName, distPath) ->
    val injectTask = tasks.register("${distTaskName}InjectPreload", InjectPreloadTask::class) {
        distDir.set(layout.buildDirectory.dir(distPath))
    }
    tasks.matching { it.name == distTaskName }.configureEach {
        finalizedBy(injectTask)
    }
}
