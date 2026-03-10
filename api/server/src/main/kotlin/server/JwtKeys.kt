/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.io.path.Path

data class JwtKeys(
    val publicKey: RSAPublicKey,
    val privateKey: RSAPrivateKey,
) {
    val algorithm: Algorithm = Algorithm.RSA256(publicKey, privateKey)
}

object JwtKeysLoader {
    private val testingKeys by lazy {
        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(4096)
        }
        val pair = generator.generateKeyPair()
        JwtKeys(
            publicKey = pair.public as RSAPublicKey,
            privateKey = pair.private as RSAPrivateKey,
        )
    }

    fun load(config: ApplicationConfig): JwtKeys {
        val testing = config.propertyOrNull("testing")?.getString()?.toBoolean() ?: false
        val privateKeyText = loadPem(config, "jwt.privateKeyPem", "jwt.privateKeyPath")
        val publicKeyText = loadPem(config, "jwt.publicKeyPem", "jwt.publicKeyPath")

        if (privateKeyText != null && publicKeyText != null) {
            return JwtKeys(
                publicKey = parsePublicKey(publicKeyText),
                privateKey = parsePrivateKey(privateKeyText),
            )
        }

        val privateKeyPath = configuredPath(config, "jwt.privateKeyPath")
        val publicKeyPath = configuredPath(config, "jwt.publicKeyPath")
        if (privateKeyPath != null || publicKeyPath != null) {
            require(privateKeyPath != null && publicKeyPath != null) {
                "Both jwt.privateKeyPath and jwt.publicKeyPath must be configured together."
            }
            ensureKeyPairFiles(privateKeyPath, publicKeyPath)
            return JwtKeys(
                publicKey = parsePublicKey(Files.readString(publicKeyPath)),
                privateKey = parsePrivateKey(Files.readString(privateKeyPath)),
            )
        }

        if (testing) {
            return testingKeys
        }

        throw IllegalStateException(
            "Missing RSA JWT keys. Configure jwt.privateKeyPath/jwt.publicKeyPath or jwt.privateKeyPem/jwt.publicKeyPem."
        )
    }

    private fun loadPem(config: ApplicationConfig, inlineKey: String, pathKey: String): String? {
        val inlineValue = config.propertyOrNull(inlineKey)?.getString()?.trim()?.takeIf { it.isNotBlank() }
        if (inlineValue != null) {
            return inlineValue
        }

        val path = configuredPath(config, pathKey) ?: return null
        return if (Files.exists(path)) Files.readString(path) else null
    }

    private fun configuredPath(config: ApplicationConfig, key: String): java.nio.file.Path? =
        config.propertyOrNull(key)?.getString()?.trim()?.takeIf { it.isNotBlank() }?.let(::Path)

    private fun ensureKeyPairFiles(privateKeyPath: java.nio.file.Path, publicKeyPath: java.nio.file.Path) {
        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            return
        }

        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(4096)
        }
        val pair = generator.generateKeyPair()

        writePem(privateKeyPath, "PRIVATE KEY", pair.private.encoded)
        writePem(publicKeyPath, "PUBLIC KEY", pair.public.encoded)
    }

    private fun writePem(path: java.nio.file.Path, type: String, encoded: ByteArray) {
        path.parent?.let(Files::createDirectories)
        Files.writeString(
            path,
            encodePem(type, encoded),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
        )
    }

    private fun parsePublicKey(pem: String): RSAPublicKey {
        val spec = X509EncodedKeySpec(decodePem(pem, "PUBLIC KEY"))
        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    private fun parsePrivateKey(pem: String): RSAPrivateKey {
        val spec = PKCS8EncodedKeySpec(decodePem(pem, "PRIVATE KEY"))
        return KeyFactory.getInstance("RSA").generatePrivate(spec) as RSAPrivateKey
    }

    private fun decodePem(pem: String, type: String): ByteArray {
        val sanitized = pem
            .replace("-----BEGIN $type-----", "")
            .replace("-----END $type-----", "")
            .replace("\\s".toRegex(), "")
        return Base64.getDecoder().decode(sanitized)
    }

    private fun encodePem(type: String, encoded: ByteArray): String {
        val base64 = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(encoded)
        return buildString {
            appendLine("-----BEGIN $type-----")
            appendLine(base64)
            appendLine("-----END $type-----")
        }
    }
}
