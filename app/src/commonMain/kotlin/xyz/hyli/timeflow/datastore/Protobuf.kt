/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.datastore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import xyz.hyli.timeflow.utils.gzipCompress
import xyz.hyli.timeflow.utils.gzipDecompress

/**
 * 通用的 ProtoBuf 反序列化函数，支持压缩和未压缩的数据
 * @param bytes 要反序列化的字节数组
 * @param defaultValue 如果反序列化失败时返回的默认值（可选）
 * @return 反序列化后的对象，如果失败则返回 defaultValue 或 null
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> ByteArray.toProtoBufData(defaultValue: T? = null): T? {
    val decompressors = listOf<(ByteArray) -> ByteArray>(
        { it.gzipDecompress() },
        { it }
    )

    for (decompressor in decompressors) {
        try {
            return ProtoBuf.decodeFromByteArray<T>(decompressor(this))
        } catch (_: Exception) {
            // 继续尝试下一个解压缩器
        }
    }

    return defaultValue
}

/**
 * 将对象序列化为 ProtoBuf 字节数组
 * @param compress 是否使用 zstd 压缩，默认为 true
 * @return 序列化后的字节数组
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> T.toProtoBufByteArray(
    compress: Boolean = true
): ByteArray = ProtoBuf.encodeToByteArray(this).let {
    if (compress) {
        it.gzipCompress()
    } else {
        it
    }
}

