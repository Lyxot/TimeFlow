/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name


object Files {
    var settingsFilePath: String? = null
    var showFileInFileManager: ((String) -> Unit)? = null
}

expect suspend fun writeBytesToFile(bytes: ByteArray, file: PlatformFile?, filename: String = file?.name ?: "file")

class SaveFileLauncher(private val launchFn: (ByteArray, String, String) -> Unit) {
    fun launch(bytes: ByteArray, name: String, extension: String) = launchFn(bytes, name, extension)
}

@Composable
expect fun rememberSaveFileLauncher(
    onSuccess: (suspend (String) -> Unit)? = null,
    onError: (suspend (Exception) -> Unit)? = null,
): SaveFileLauncher

@Composable
fun rememberOpenFileLauncher(
    extension: String,
    onResult: (PlatformFile?) -> Unit,
) = rememberFilePickerLauncher(
    type = FileKitType.File(extension),
    onResult = onResult,
)

@Composable
fun rememberOpenImageLauncher(
    onResult: (PlatformFile?) -> Unit,
) = rememberFilePickerLauncher(
    type = FileKitType.Image,
    onResult = onResult,
)