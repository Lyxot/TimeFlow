/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import io.ktor.server.config.*
import xyz.hyli.timeflow.server.JwtKeysLoader
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertTrue

class JwtKeysLoaderTest {
    @Test
    fun `generate key files when configured paths are missing`() {
        val tempDir = createTempDirectory("jwt-keys-test")
        val privateKeyPath = tempDir.resolve("jwt-private.pem")
        val publicKeyPath = tempDir.resolve("jwt-public.pem")
        val config = MapApplicationConfig(
            "jwt.privateKeyPath" to privateKeyPath.toString(),
            "jwt.publicKeyPath" to publicKeyPath.toString(),
            "jwt.issuer" to "test-issuer",
            "jwt.audience" to "test-audience",
            "jwt.realm" to "test-realm",
        )

        val keys = JwtKeysLoader.load(config)

        assertTrue(privateKeyPath.exists())
        assertTrue(publicKeyPath.exists())
        assertTrue(keys.privateKey.encoded.isNotEmpty())
        assertTrue(keys.publicKey.encoded.isNotEmpty())
    }
}
