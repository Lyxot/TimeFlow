/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

sealed class BuildType {
    abstract val capitalized: String

    object Debug : BuildType() {
        override fun toString() = "debug"
        override val capitalized = this.toString().capitalize()
    }

    object Release : BuildType() {
        override fun toString() = "release"
        override val capitalized = this.toString().capitalize()
    }

    companion object {
        val all = listOf(Debug, Release)
    }

    fun isDebug() = this is Debug
    fun isRelease() = this is Release
}