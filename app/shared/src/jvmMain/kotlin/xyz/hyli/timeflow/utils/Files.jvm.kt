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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch

actual suspend fun writeBytesToFile(bytes: ByteArray, file: PlatformFile?, filename: String) {
    file?.write(bytes)
}

@Composable
actual fun rememberSaveFileLauncher(
    onSuccess: (suspend (String) -> Unit)?,
    onError: (suspend (Exception) -> Unit)?,
): SaveFileLauncher {
    val scope = rememberCoroutineScope()
    val pendingBytes = remember { arrayOfNulls<ByteArray>(1) }
    val nativeSaver = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        val bytes = pendingBytes[0] ?: return@rememberFileSaverLauncher
        pendingBytes[0] = null
        if (file != null) {
            scope.launch {
                try {
                    file write bytes
                    onSuccess?.invoke(file.name)
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }
    }
    return remember(nativeSaver) {
        SaveFileLauncher { bytes, name, extension ->
            pendingBytes[0] = bytes
            nativeSaver.launch(name, extension)
        }
    }
}