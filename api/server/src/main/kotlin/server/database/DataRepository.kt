/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.database

import xyz.hyli.timeflow.api.models.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 数据仓库的接口，定义了所有数据库操作。
 */
@OptIn(ExperimentalUuidApi::class)
interface DataRepository {
    /**
     * 根据认证ID查找用户。
     * @param authId 来自认证系统（如JWT）的用户唯一ID。
     * @return 如果找到则返回 [User] 对象，否则返回 null。
     */
    suspend fun findUserByAuthId(authId: Uuid): User?

    /**
     * 根据邮箱地址查找用户。
     * @param email 要查找的用户邮箱。
     * @return 如果找到则返回 [User] 对象，否则返回 null。
     */
    suspend fun findUserByEmail(email: String): User?

    /**
     * 根据邮箱地址查找用户的密码哈希。
     * @param email 要查找的用户邮箱。
     * @return 如果找到则返回密码哈希字符串，否则返回 null。
     */
    suspend fun findPasswordHashByEmail(email: String): Pair<String, User>?

    /**
     * 创建一个新用户。
     * @param authId 新用户的认证ID。
     * @param username 新用户的用户名。
     * @param email 新用户的邮箱。
     * @param passwordHash 经过哈希处理的密码。
     * @return 创建成功后的 [User] 对象。
     */
    suspend fun createUser(authId: Uuid, username: String, email: String, passwordHash: String): User

    /**
     * Adds a new refresh token JTI to the database.
     * @param userId The ID of the user owning the token.
     * @param jti The unique ID (JTI) of the JWT refresh token.
     * @param expiresAt The expiration timestamp of the refresh token.
     */
    suspend fun addRefreshToken(userId: Int, jti: Uuid, expiresAt: java.time.Instant): RefreshTokenEntity

    /**
     * Checks if a given refresh token JTI is valid (i.e., exists in the database).
     * @param jti The JTI to check.
     * @return True if the JTI is found, false otherwise.
     */
    suspend fun isRefreshTokenValid(userId: Int, jti: Uuid): Boolean

    /**
     * Revokes a refresh token by deleting its JTI from the database.
     * @param jti The JTI of the token to revoke.
     */
    suspend fun revokeRefreshToken(jti: Uuid, delete: Boolean = false)
}
