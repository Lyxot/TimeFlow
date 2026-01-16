/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    /**
     * 初始化数据库连接。
     * 此方法应在 Ktor 应用启动时调用。
     * @param config Ktor 的应用配置，用于读取生产数据库的连接参数。
     */
    fun init(config: ApplicationConfig) {
        // testing 如果为 true，则使用 H2 内存数据库进行测试；否则，使用配置文件中的 PostgreSQL。
        val testing = config.propertyOrNull("testing")?.getString()?.toBoolean() ?: false
        val db = if (testing) {
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        } else {
            val url = config.property("postgres.url").getString()
            val user = config.property("postgres.user").getString()
            val password = config.property("postgres.password").getString()
            Database.connect(createHikariDataSource(url, user, password))
        }

        // 在一个事务中，创建所有数据表（如果它们不存在的话）
        transaction(db) {
            SchemaUtils.create(
                UsersTable,
                RefreshTokensTable,
                VerificationCodesTable,
                SchedulesTable,
                CoursesTable,
            )
        }
    }

    /**
     * 创建并配置 HikariCP 数据源作为连接池。
     */
    private fun createHikariDataSource(
        url: String,
        user: String,
        password: String
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = url
        username = user
        this.password = password
        maximumPoolSize = 3 // TODO: 在配置文件中调整
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    /**
     * A helper function to execute database queries in a suspended transaction
     * on an I/O-optimized dispatcher.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }
}