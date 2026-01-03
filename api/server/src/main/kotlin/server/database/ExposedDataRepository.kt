/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import xyz.hyli.timeflow.api.models.User
import xyz.hyli.timeflow.server.database.DatabaseFactory.dbQuery
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * DataRepository 接口的 Exposed 实现，完全使用 DAO 模式。
 */
@OptIn(ExperimentalUuidApi::class)
class ExposedDataRepository : DataRepository {

    override suspend fun findUserByAuthId(authId: Uuid): User? = dbQuery {
        UserEntity
            .find { UsersTable.authId eq authId.toJavaUuid() }
            .singleOrNull()
            ?.user
    }

    override suspend fun findUserByEmail(email: String): User? = dbQuery {
        UserEntity
            .find { UsersTable.email eq email }
            .singleOrNull()
            ?.user
    }

    override suspend fun findPasswordHashByEmail(email: String): Pair<String, User>? = dbQuery {
        // 使用 DAO 模式查找实体，然后只返回 passwordHash 属性
        UserEntity
            .find { UsersTable.email eq email }
            .singleOrNull()
            ?.let {
                Pair(it.passwordHash, it.user)
            }
    }

    override suspend fun createUser(authId: Uuid, username: String, email: String, passwordHash: String): User =
        dbQuery {
            UserEntity.new {
                this.authId = authId.toJavaUuid()
                this.username = username
                this.email = email
                this.passwordHash = passwordHash
            }.user
        }

    override suspend fun addRefreshToken(userId: Int, jti: Uuid, expiresAt: java.time.Instant) = dbQuery {
        RefreshTokenEntity.new {
            this.user = UserEntity[userId]
            this.jti = jti.toJavaUuid()
            this.expiresAt = expiresAt.toKotlinInstant()
        }
    }


    override suspend fun isRefreshTokenValid(userId: Int, jti: Uuid): Boolean = dbQuery {
        RefreshTokenEntity
            .find {
                (RefreshTokensTable.userId eq userId) and
                        (RefreshTokensTable.jti eq jti.toJavaUuid())
            }
            .firstOrNull()
            ?.expiresAt
            .let {
                if (it == null) false
                else {
                    val result = it > Clock.System.now()
                    if (!result) {
                        revokeRefreshToken(jti)
                    }
                    result
                }
            }
    }

    override suspend fun revokeRefreshToken(jti: Uuid, delete: Boolean) {
        dbQuery {
            RefreshTokenEntity.find { RefreshTokensTable.jti eq jti.toJavaUuid() }.firstOrNull()?.let {
                if (delete) {
                    it.delete()
                } else {
                    // 1s 后过期
                    it.expiresAt = Clock.System.now().plus(1.seconds)
                }
            }
        }
    }
}
