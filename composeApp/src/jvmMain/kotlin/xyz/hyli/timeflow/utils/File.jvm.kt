/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import java.awt.Desktop

actual fun showFileInFileManager(path: String) {
    val file = java.io.File(path)
    if (!file.exists()) return

    try {
        Desktop.getDesktop().browseFileDirectory(file)
    } catch (e: Exception) {
        Desktop.getDesktop().open(file.parentFile)
    }
}