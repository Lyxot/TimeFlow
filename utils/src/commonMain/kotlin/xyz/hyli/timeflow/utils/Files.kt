/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name


object Files {
    var settingsFilePath: String? = null
    var showFileInFileManager: ((String) -> Unit)? = null
}

expect suspend fun writeBytesToFile(bytes: ByteArray, file: PlatformFile?, filename: String = file?.name ?: "file")