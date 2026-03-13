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
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import org.slf4j.LoggerFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.io.path.Path

data class JwtKeys(
    val publicKey: ECPublicKey,
    val privateKey: ECPrivateKey,
) {
    val algorithm: Algorithm = Algorithm.ECDSA256(publicKey, privateKey)
}

object JwtKeysLoader {
    private val log = LoggerFactory.getLogger(JwtKeysLoader::class.java)

    private val testingKeys by lazy {
        val pair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        JwtKeys(
            publicKey = pair.public as ECPublicKey,
            privateKey = pair.private as ECPrivateKey,
        )
    }

    fun load(config: ApplicationConfig): JwtKeys {
        val testing = isTestMode
        val privateKeyText = loadPem(config, "jwt.privateKeyPem", "jwt.privateKeyPath")
        val publicKeyText = loadPem(config, "jwt.publicKeyPem", "jwt.publicKeyPath")

        if (privateKeyText != null && publicKeyText != null) {
            return loadAndValidate(publicKeyText, privateKeyText)
        }

        val privateKeyPath = configuredPath(config, "jwt.privateKeyPath")
        val publicKeyPath = configuredPath(config, "jwt.publicKeyPath")
        if (privateKeyPath != null || publicKeyPath != null) {
            require(privateKeyPath != null && publicKeyPath != null) {
                "Both jwt.privateKeyPath and jwt.publicKeyPath must be configured together."
            }
            ensureKeyPairFiles(privateKeyPath, publicKeyPath)
            return loadAndValidate(Files.readString(publicKeyPath), Files.readString(privateKeyPath))
        }

        if (testing) {
            return testingKeys
        }

        throw IllegalStateException(
            "Missing EC JWT keys. Configure jwt.privateKeyPath/jwt.publicKeyPath or jwt.privateKeyPem/jwt.publicKeyPem."
        )
    }

    private fun loadAndValidate(publicKeyPem: String, privateKeyPem: String): JwtKeys {
        val publicKey = try {
            parsePublicKey(publicKeyPem)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse JWT public key. Ensure it is a valid EC (P-256) public key in PEM format.", e)
        }
        val privateKey = try {
            parsePrivateKey(privateKeyPem)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse JWT private key. Ensure it is a valid EC (P-256) private key in PEM format.", e)
        }
        val keys = JwtKeys(publicKey, privateKey)
        try {
            val token = com.auth0.jwt.JWT.create().withClaim("_test", true).sign(keys.algorithm)
            com.auth0.jwt.JWT.require(keys.algorithm).build().verify(token)
        } catch (e: Exception) {
            throw IllegalStateException("JWT key pair validation failed. The public and private keys do not match.", e)
        }
        return keys
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
        val privateExists = Files.exists(privateKeyPath)
        val publicExists = Files.exists(publicKeyPath)

        if (privateExists && publicExists) {
            return
        }

        if (privateExists || publicExists) {
            throw IllegalStateException(
                "Only one JWT key file exists. Both jwt.privateKeyPath and jwt.publicKeyPath must be present, or neither (to auto-generate)."
            )
        }

        log.warn("JWT key files not found at {} and {}. Auto-generating new ES256 key pair. All existing tokens will be invalidated.", privateKeyPath, publicKeyPath)

        val pair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()

        writePem(privateKeyPath, "PRIVATE KEY", pair.private.encoded)
        writePem(publicKeyPath, "PUBLIC KEY", pair.public.encoded)
    }

    private val isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix")

    private fun writePem(path: java.nio.file.Path, type: String, encoded: ByteArray) {
        path.parent?.let(Files::createDirectories)
        if (isPosix && !Files.exists(path)) {
            Files.createFile(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
        }
        Files.writeString(
            path,
            encodePem(type, encoded),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
        )
    }

    private fun parsePublicKey(pem: String): ECPublicKey {
        val spec = X509EncodedKeySpec(decodePem(pem, "PUBLIC KEY"))
        return KeyFactory.getInstance("EC").generatePublic(spec) as ECPublicKey
    }

    private fun parsePrivateKey(pem: String): ECPrivateKey {
        val spec = PKCS8EncodedKeySpec(decodePem(pem, "PRIVATE KEY"))
        return KeyFactory.getInstance("EC").generatePrivate(spec) as ECPrivateKey
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
