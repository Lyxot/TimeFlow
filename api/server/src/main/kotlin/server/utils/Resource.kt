/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.utils

import java.net.URI
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.exists

internal fun resolveZipPath(path: String): Path? {
    val filePath = Path.of(path)
    return filePath.takeIf { it.exists() }
}

internal fun resolveBundledZipPath(resourcePath: String): Path? {
    val resourceUri = object {}.javaClass.getResource(resourcePath)?.toURI() ?: return null
    return resourceUri.toBundledPath()
}

private fun URI.toBundledPath(): Path? = when (scheme) {
    "file" -> Path.of(this)
    "jar" -> {
        try {
            FileSystems.newFileSystem(this, emptyMap<String, Any>())
        } catch (_: FileSystemAlreadyExistsException) {
        }
        runCatching { Path.of(this) }.getOrNull()
    }

    else -> null
}
