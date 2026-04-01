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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.launch

actual suspend fun writeBytesToFile(bytes: ByteArray, file: PlatformFile?, filename: String) =
    FileKit.download(bytes, filename)

@Composable
actual fun rememberSaveFileLauncher(
    onSuccess: (suspend (String) -> Unit)?,
    onError: (suspend (Exception) -> Unit)?,
): SaveFileLauncher {
    val scope = rememberCoroutineScope()
    return remember {
        SaveFileLauncher { bytes, name, extension ->
            scope.launch {
                try {
                    FileKit.download(bytes, "$name.$extension")
                    onSuccess?.invoke("$name.$extension")
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }
    }
}