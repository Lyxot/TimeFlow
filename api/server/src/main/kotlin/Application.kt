/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import io.ktor.server.application.*
import io.ktor.server.netty.*
import xyz.hyli.timeflow.server.*
import xyz.hyli.timeflow.server.database.DatabaseFactory
import xyz.hyli.timeflow.server.database.ExposedDataRepository

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 从配置中读取 `embedded` 标志来决定使用哪个数据库
    // 如果在测试中运行，可以设置 embedded=true 来使用 H2 内存数据库
    val embedded = environment.config.propertyOrNull("postgres.embedded")?.getString()?.toBoolean() ?: false
    DatabaseFactory.init(testing = embedded, config = environment.config)
    val repository = ExposedDataRepository()

    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureAdministration()
    configureSecurity(repository)
    configureRouting(repository)
}
