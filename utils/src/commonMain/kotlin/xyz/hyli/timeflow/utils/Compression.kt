/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress

fun ByteArray.gzipCompress(): ByteArray = this.compress(
    method = GZIP
)

fun ByteArray.gzipDecompress(): ByteArray =
    this.uncompress(
        method = GZIP
    )