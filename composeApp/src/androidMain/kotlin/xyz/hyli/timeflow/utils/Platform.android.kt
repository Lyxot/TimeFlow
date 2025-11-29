/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

import android.os.Build

internal actual fun currentPlatformImpl(): Platform {
    val supportDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    if (Build.SUPPORTED_ABIS == null) {
        // unit testing
        return Platform.Android(Arch.ARMV8A, supportDynamicColor)
    }
    return Build.SUPPORTED_ABIS.getOrNull(0)?.let { abi ->
        when (abi.lowercase()) {
            "armeabi-v7a" -> Platform.Android(Arch.ARMV7A, supportDynamicColor)
            "arm64-v8a" -> Platform.Android(Arch.ARMV8A, supportDynamicColor)
            "x86_64" -> Platform.Android(Arch.X86_64, supportDynamicColor)
            else -> Platform.Android(Arch.ARMV8A, supportDynamicColor)
        }
    } ?: Platform.Android(Arch.ARMV8A, supportDynamicColor)
}