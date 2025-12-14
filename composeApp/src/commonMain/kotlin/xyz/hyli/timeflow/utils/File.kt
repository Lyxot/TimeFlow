/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import okio.Buffer
import okio.GzipSink
import okio.GzipSource
import okio.buffer
import okio.use

expect fun showFileInFileManager(path: String)

fun ByteArray.gzipCompress(): ByteArray {
    val buffer = Buffer()
    GzipSink(buffer).buffer().use { gzipSink ->
        gzipSink.write(this)
    }
    return buffer.readByteArray()
}

fun ByteArray.gzipDecompress(): ByteArray {
    val sourceBuffer = Buffer().write(this)
    GzipSource(sourceBuffer).buffer().use { gzipSource ->
        return gzipSource.readByteArray()
    }
}
