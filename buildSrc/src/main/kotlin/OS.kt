/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

sealed class OS {
    abstract val name: String

    open class Desktop(override val name: String) : OS() {
        object MacOS : Desktop("macos")
        object Windows : Desktop("windows")
        object Linux : Desktop("linux")
    }

    open class Mobile(override val name: String) : OS() {
        object Android : Mobile("android")
        object Ios : Mobile("ios")
    }

    open class Web(override val name: String) : OS() {
        object Js : Web("js")
        object WasmJs : Web("wasmJs")
        object WebCompat : Web("webCompat")
    }
}