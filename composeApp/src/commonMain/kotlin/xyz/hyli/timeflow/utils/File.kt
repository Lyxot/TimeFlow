/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import com.squareup.zstd.okio.zstdCompress
import com.squareup.zstd.okio.zstdDecompress
import okio.Buffer
import okio.buffer
import okio.use

expect fun showFileInFileManager(path: String)

fun ByteArray.zstdCompress(): ByteArray {
    val buffer = Buffer()
    // 使用 zstdCompress 包装目标 buffer
    buffer.zstdCompress().buffer().use { sink ->
        sink.write(this)
    }
    return buffer.readByteArray()
}

fun ByteArray.zstdDecompress(): ByteArray {
    val buffer = Buffer().write(this)
    // 使用 zstdDecompress 包装源 buffer
    buffer.zstdDecompress().buffer().use { source ->
        return source.readByteArray()
    }
}