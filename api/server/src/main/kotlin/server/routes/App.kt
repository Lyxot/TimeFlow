/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.server.utils.resolveBundledZipPath
import xyz.hyli.timeflow.server.utils.resolveZipPath
import kotlin.io.path.name
import kotlin.io.path.pathString

private val FINGERPRINTED_REGEX =
    Regex(".*[0-9a-f]{16,}.*\\.(js|wasm|css|png|jpg|jpeg|webp|svg|mjs)$", RegexOption.IGNORE_CASE)

fun Route.appRoutes(config: ApplicationConfig, log: Logger) {
    val serveWebApp = config.property("webApp.serveEnabled").getString().toBoolean()
    if (!serveWebApp) {
        log.info("Web app serving is disabled")
        return
    }

    val location = config.property("webApp.location").getString().trim('/')
    val configuredZipPath = config.property("webApp.zipPath").getString().takeIf { it.isNotBlank() }
    val appZipPath = when {
        configuredZipPath != null -> resolveZipPath(configuredZipPath)
        BuildConfig.BUNDLED_WEB_APP_ZIP -> resolveBundledZipPath("/static/app.zip")
        else -> null
    }

    if (appZipPath == null) {
        val expectedLocation = configuredZipPath ?: "/static/app.zip"
        log.warn("Web app serving is enabled but no zip was found at '{}'", expectedLocation)
        return
    }

    log.info("Serving web app at '/{}'", location)

    staticZip("/$location/", "TimeFlow", appZipPath) {
        enableAutoHeadResponse()
        etag(ETagProvider.StrongSha256)
        cacheControl { file ->
            val name = file.name

            val isHtml = name.endsWith(".html", ignoreCase = true)
            val isManifest = name.equals("manifest.json", ignoreCase = true)

            // 形如 e780302d66126f393091.wasm / b3831c7563128a6b300a.wasm 的“指纹文件”
            // 这里用：包含 >=16 位十六进制串 + 常见扩展名 来判断
            val isFingerprinted = FINGERPRINTED_REGEX.matches(name)

            val isSourceMap = name.endsWith(".map", ignoreCase = true)
            val isLicenseTxt = name.endsWith(".LICENSE.txt", ignoreCase = true)

            val isIcon =
                name.equals("favicon.ico", true) ||
                        name.startsWith("favicon-", true) ||
                        name.startsWith("android-chrome-", true) ||
                        name.equals("apple-touch-icon.png", true)

            val isComposeResources = file.pathString.contains("composeResources")

            val isFont =
                isComposeResources &&
                        (name.endsWith(".ttf", true) || name.endsWith(".otf", true) || name.endsWith(".woff2", true))

            // 这些是固定文件名但往往会随版本变化的“核心运行时文件”
            val isCoreFixedRuntime =
                name.equals("web.js", true) ||
                        name.equals("skiko.wasm", true) ||
                        name.equals("skiko.mjs", true) ||
                        name.equals("skikod8.mjs", true) ||
                        name.equals("originJsWeb.js", true) ||
                        name.equals("originWasmWeb.js", true) ||
                        name.endsWith(".mjs", true)

            val maxAgeSeconds = when {
                // 入口 HTML/manifest：更新要及时；依赖 ETag/Last-Modified 走 304
                isHtml || isManifest -> null

                // 指纹资源：30天 + immutable
                isFingerprinted -> 2592000

                // sourcemap：一般只排障用，别太久
                isSourceMap -> 3600

                // LICENSE 文件：随包走，缓存一天就行
                isLicenseTxt -> 86400

                // 图标：一天（想更激进可 7d）
                isIcon -> 86400

                // 字体：通常很大且很少变，30 天
                isFont -> 2592000

                // composeResources 其他：一天
                isComposeResources -> 86400

                // 固定名核心运行时：短缓存 1h（避免更新卡太久）
                isCoreFixedRuntime -> 3600

                // 其他静态文件：给个默认短缓存
                else -> 3600
            }

            val result: MutableList<CacheControl> = mutableListOf()
            if (isHtml || isManifest) result += CacheControl.NoCache(null)
            if (isFingerprinted) result += object : CacheControl(null) {
                override fun toString(): String = "immutable"
            }
            maxAgeSeconds?.let { result += CacheControl.MaxAge(it, visibility = CacheControl.Visibility.Public) }
            result
        }
    }

    if (location.isNotEmpty()) {
        route("/$location") {
            // Redirect /{location} to /{location}/
            handle {
                call.respondRedirect("/$location/")
            }
        }
    }
}
