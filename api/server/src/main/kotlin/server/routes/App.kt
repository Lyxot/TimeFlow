/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.hyli.timeflow.server.utils.extractResourceToTemp

fun Route.appRoutes() {
    // Serve app
    val appZipPath = extractResourceToTemp("/static/app.zip")
    staticZip("/app/", "TimeFlow", appZipPath) {
        enableAutoHeadResponse()
        etag(ETagProvider.StrongSha256)
    }

    route("/app") {
        // Redirect /app to /app/
        handle {
            call.respondRedirect("/app/")
        }
    }
}