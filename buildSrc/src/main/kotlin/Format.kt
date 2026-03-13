/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

sealed class Format {
    abstract val suffix: String

    open class MacOS(override val suffix: String) : Format() {
        object Dmg : MacOS("dmg")
    }

    open class Windows(override val suffix: String) : Format() {
        object Msi : Windows("msi")
        object Exe : Windows("exe")
        object Zip : Windows("zip")
    }

    open class Linux(override val suffix: String) : Format() {
        object Deb : Linux("deb")
        object Rpm : Linux("rpm")
        object AppImage : Linux("AppImage")
    }

    open class Mobile(override val suffix: String) : Format() {
        object Apk : Mobile("apk")
        object Ipa : Mobile("ipa")
    }

    object Zip : Format() {
        override val suffix: String = "zip"
    }

    object Jar : Format() {
        override val suffix: String = "jar"
    }
}