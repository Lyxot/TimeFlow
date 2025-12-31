/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.routes

import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.api.models.Ping
import xyz.hyli.timeflow.api.models.Version

fun Route.utilRoutes() {
    get<Ping> {
        call.respond(Ping.Response())
    }
    get<Version> {
        call.respond(
            Version.Response(
                version = BuildConfig.APP_VERSION_NAME,
                buildTime = BuildConfig.BUILD_TIME,
                commitHash = BuildConfig.GIT_COMMIT_HASH
            )
        )
    }
}
