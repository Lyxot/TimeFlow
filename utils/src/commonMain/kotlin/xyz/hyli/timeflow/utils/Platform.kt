/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

// https://github.com/open-ani/animeko/blob/main/utils/platform/src/commonMain/kotlin/Platform.kt

package xyz.hyli.timeflow.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class Platform {
    abstract val name: String // don't change, it's actually an ID

    final override fun toString(): String = name

    ///////////////////////////////////////////////////////////////////////////
    // mobile
    ///////////////////////////////////////////////////////////////////////////

    sealed class Mobile : Platform()

    data class Android(
        val supportDynamicColor: Boolean,
    ) : Mobile() {
        override val name: String get() = "Android"
    }

    data object Ios : Mobile() {
        override val name: String get() = "iOS"
    }


    ///////////////////////////////////////////////////////////////////////////
    // desktop
    ///////////////////////////////////////////////////////////////////////////

    sealed class Desktop(
        override val name: String
    ) : Platform()

    class Windows : Desktop("Windows")

    class MacOS : Desktop("macOS")

    class Linux : Desktop("Linux")

    data class Wasm(
        val userAgent: String,
    ) : Platform() {
        override val name: String get() = "Wasm"
    }
}


@Suppress("ObjectPropertyName")
private val _currentPlatform = runCatching { currentPlatformImpl() } // throw only on get

/**
 * 获取当前的平台. 在 Linux 上使用时会抛出 [UnsupportedOperationException].
 *
 * CI 会跑 Ubuntu test (比较快), 所以在 test 环境需要谨慎使用此 API.
 */

fun currentPlatform(): Platform = _currentPlatform.getOrThrow()


fun currentPlatformDesktop(): Platform.Desktop {
    val platform = currentPlatform()
    check(platform is Platform.Desktop)
    return platform
}

internal expect fun currentPlatformImpl(): Platform

@OptIn(ExperimentalContracts::class)
inline fun Platform.isDesktop(): Boolean {
    contract { returns(true) implies (this@isDesktop is Platform.Desktop) }
    return this is Platform.Desktop
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isMacOS(): Boolean {
    contract { returns(true) implies (this@isMacOS is Platform.MacOS) }
    return this is Platform.MacOS
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isWindows(): Boolean {
    contract { returns(true) implies (this@isWindows is Platform.Windows) }
    return this is Platform.Windows
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isIos(): Boolean {
    contract { returns(true) implies (this@isIos is Platform.Ios) }
    return this is Platform.Ios
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isMobile(): Boolean {
    contract { returns(true) implies (this@isMobile is Platform.Mobile) }
    return this is Platform.Mobile
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isAndroid(): Boolean {
    contract { returns(true) implies (this@isAndroid is Platform.Android) }
    return this is Platform.Android
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isWasm(): Boolean {
    contract { returns(true) implies (this@isWasm is Platform.Wasm) }
    return this is Platform.Wasm
}

inline fun Platform.supportDynamicColor(): Boolean {
    return when (this) {
        is Platform.Android -> this.supportDynamicColor
        is Platform.Windows -> true
        else -> false
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Platform.isLinux(): Boolean {
    contract { returns(true) implies (this@isLinux is Platform.Linux) }
    return this is Platform.Linux
}

inline fun Platform.hasScrollingBug() = isDesktop()