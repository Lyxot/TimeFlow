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
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import xyz.hyli.timeflow.server.isTestMode

object DatabaseFactory {
    private var dataSource: HikariDataSource? = null

    /**
     * 初始化数据库连接。
     * 此方法应在 Ktor 应用启动时调用。
     * @param config Ktor 的应用配置，用于读取数据库的连接参数。
     */
    fun init(config: ApplicationConfig) {
        val host = config.property("postgres.host").getString()
        val port = config.property("postgres.port").getString()
        val database = config.property("postgres.database").getString()
        val user = config.property("postgres.user").getString()
        val password = config.property("postgres.password").getString()
        val maximumPoolSize = config.propertyOrNull("postgres.maximumPoolSize")?.getString()?.toInt() ?: 10
        val url = "jdbc:postgresql://$host:$port/$database"
        val hikariDataSource = createHikariDataSource(url, user, password, maximumPoolSize)
        dataSource = hikariDataSource
        migratePostgres(hikariDataSource)
        Database.connect(hikariDataSource)
    }

    private fun migratePostgres(dataSource: HikariDataSource) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .cleanDisabled(!isTestMode)
            .load()
        if (isTestMode) {
            flyway.clean()
        }
        flyway.migrate()
    }

    /**
     * 创建并配置 HikariCP 数据源作为连接池。
     */
    private fun createHikariDataSource(
        url: String,
        user: String,
        password: String,
        maximumPoolSize: Int
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = url
        username = user
        this.password = password
        this.maximumPoolSize = maximumPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_READ_COMMITTED"
        validate()
    })

    /**
     * Returns true if the database connection pool is running and a connection can be obtained.
     */
    fun isHealthy(): Boolean = try {
        dataSource?.connection?.use { it.isValid(2) } ?: false
    } catch (_: Exception) {
        false
    }

    /**
     * Closes the database connection pool.
     * Should be called during application shutdown.
     */
    fun close() {
        dataSource?.close()
        dataSource = null
    }

    /**
     * A helper function to execute database queries in a suspended transaction
     * on an I/O-optimized dispatcher.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }
}
