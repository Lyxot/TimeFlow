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