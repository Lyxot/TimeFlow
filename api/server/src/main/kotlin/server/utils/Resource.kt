/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server.utils

import java.nio.file.Files
import java.nio.file.Path

/**
 * Extracts a resource file to a temporary location and returns its path.
 * @param resourcePath Path to the resource (e.g., "/static/TimeFlow-wasmJs.zip")
 * @return Path to the extracted temporary file
 */
internal fun extractResourceToTemp(resourcePath: String): Path {
    val inputStream = object {}.javaClass.getResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Resource not found: $resourcePath")

    val tempFile = Files.createTempFile("timeflow-", resourcePath.substringAfterLast("."))
    tempFile.toFile().deleteOnExit()

    inputStream.use { input ->
        Files.newOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }

    return tempFile
}
