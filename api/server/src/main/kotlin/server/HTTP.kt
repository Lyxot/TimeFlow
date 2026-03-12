/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*

fun Application.configureHTTP() {
    val config = environment.config

    // Reject request bodies larger than 1 MB
    intercept(ApplicationCallPipeline.Plugins) {
        val contentLength = call.request.contentLength()
        if (contentLength != null && contentLength > 10_485_760L) {
            call.respondText(
                """{"error":"Request body too large"}""",
                ContentType.Application.Json,
                HttpStatusCode.PayloadTooLarge
            )
            finish()
        }
    }

    install(Compression) {
        gzip()
        deflate()
    }
    install(ConditionalHeaders)
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        header(HttpHeaders.XRequestId)
    }
    if (config.booleanProperty("http.forwardedHeaders.enabled")) {
        install(ForwardedHeaders)
    }
    if (config.booleanProperty("http.xForwardedHeaders.enabled")) {
        install(XForwardedHeaders)
    }

    if (config.booleanProperty("http.cors.enabled")) {
        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.XRequestId)
            allowNonSimpleContentTypes = true
            allowCredentials = config.booleanProperty("http.cors.allowCredentials")

            if (config.booleanProperty("http.cors.anyHost")) {
                anyHost()
            } else {
                config.csvProperty("http.cors.allowedHosts").forEach { host ->
                    allowHost(host, listOf("http", "https"))
                }
            }
        }
    }

    if (config.booleanProperty("http.hsts.enabled")) {
        install(HSTS) {
            includeSubDomains = config.booleanProperty("http.hsts.includeSubDomains")
            preload = config.booleanProperty("http.hsts.preload")
            maxAgeInSeconds = config.longProperty("http.hsts.maxAgeInSeconds")
        }
    }

    if (config.booleanProperty("http.httpsRedirect.enabled")) {
        install(HttpsRedirect) {
            sslPort = config.intProperty("http.httpsRedirect.sslPort")
            permanentRedirect = config.booleanProperty("http.httpsRedirect.permanentRedirect")
        }
    }
}

private fun ApplicationConfig.booleanProperty(path: String): Boolean =
    property(path).getString().toBoolean()

private fun ApplicationConfig.intProperty(path: String): Int =
    property(path).getString().toInt()

private fun ApplicationConfig.longProperty(path: String): Long =
    property(path).getString().toLong()

private fun ApplicationConfig.csvProperty(path: String): List<String> =
    property(path).getString()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
