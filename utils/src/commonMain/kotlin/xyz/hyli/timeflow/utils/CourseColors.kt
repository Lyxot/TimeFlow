/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

/**
 * Material Design color palette for course cards (ARGB Int values).
 * These are the raw color values used by both Compose UI and non-UI modules.
 */
object CourseColors {
    const val RED = 0xFFF44336.toInt()
    const val PINK = 0xFFE91E63.toInt()
    const val PURPLE = 0xFF9C27B0.toInt()
    const val DEEP_PURPLE = 0xFF673AB7.toInt()
    const val INDIGO = 0xFF3F51B5.toInt()
    const val BLUE = 0xFF2196F3.toInt()
    const val LIGHT_BLUE = 0xFF03A9F4.toInt()
    const val CYAN = 0xFF00BCD4.toInt()
    const val TEAL = 0xFF009688.toInt()
    const val GREEN = 0xFF4CAF50.toInt()
    const val LIGHT_GREEN = 0xFF8BC34A.toInt()
    const val LIME = 0xFFCDDC39.toInt()
    const val YELLOW = 0xFFFFEB3B.toInt()
    const val AMBER = 0xFFFFC107.toInt()
    const val ORANGE = 0xFFFF9800.toInt()
    const val DEEP_ORANGE = 0xFFFF5722.toInt()
    const val BROWN = 0xFF795548.toInt()
    const val GRAY = 0xFF9E9E9E.toInt()
    const val BLUE_GRAY = 0xFF607D8B.toInt()

    val ALL = intArrayOf(
        RED, PINK, PURPLE, DEEP_PURPLE,
        INDIGO, BLUE, LIGHT_BLUE, CYAN,
        TEAL, GREEN, LIGHT_GREEN, LIME,
        YELLOW, AMBER, ORANGE, DEEP_ORANGE,
        BROWN, GRAY, BLUE_GRAY
    )

    fun random(): Int = ALL.random()
}
