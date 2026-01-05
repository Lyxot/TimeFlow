/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

internal actual fun currentPlatformImpl(): Platform {
    val os = System.getProperty("os.name")?.lowercase() ?: error("Cannot determine platform, 'os.name' is null.")
    return when {
        "mac" in os || "os x" in os || "darwin" in os -> Platform.MacOS()
        "windows" in os -> Platform.Windows()
//        "linux" in os || "redhat" in os || "debian" in os || "ubuntu" in os -> Platform.Linux(arch)
        "linux" in os -> Platform.Linux()
        else -> throw UnsupportedOperationException("Unsupported platform: $os")
    }
}
