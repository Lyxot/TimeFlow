/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

import org.apache.tools.ant.taskdefs.condition.Os

sealed class Target {
    companion object {
        const val APP_NAME = "TimeFlow"
        lateinit var appVersion: String
    }

    abstract val format: Format
    abstract val artifactSuffix: String
    val artifactName: String
        get() = listOf(
            APP_NAME, appVersion, artifactSuffix,
        ).filter { it.isNotBlank() }.joinToString("-") + ".${format.suffix}"

    sealed class App : Target() {
        abstract val system: OS
    }

    sealed class Mobile : App() {
        abstract override val system: OS.Mobile
        abstract override val format: Format.Mobile
        override val artifactSuffix = ""
    }

    object Android : Mobile() {
        override val system: OS.Mobile = OS.Mobile.Android
        override val format: Format.Mobile = Format.Mobile.Apk
    }

    object Ios : Mobile() {
        override val system: OS.Mobile = OS.Mobile.Ios
        override val format: Format.Mobile = Format.Mobile.Ipa
    }

    sealed class Desktop : App() {
        abstract override val system: OS.Desktop
        val arch: Arch = System.getProperty("os.arch").let {
            when {
                it.contains("64") && (it.contains("arm") || it.contains("aarch")) -> Arch.Arm64
                it.contains("64") && (it.contains("x86") || it.contains("amd")) -> Arch.X86_64
                else -> throw IllegalArgumentException("Unsupported architecture: $it")
            }
        }
        abstract override val format: Format
        val archString
            get() = when (system) {
                OS.Desktop.Windows -> when (arch) {
                    Arch.X86_64 -> "x86_64"
                    Arch.Arm64 -> "arm64"
                }

                else -> when (arch) {
                    Arch.X86_64 -> "x86_64"
                    Arch.Arm64 -> "aarch64"
                }
            }
        override val artifactSuffix get() = "${system.name}-$archString"

        fun matchCurrentSystem(): Boolean {
            return when (this.system) {
                is OS.Desktop.MacOS -> Os.isFamily(Os.FAMILY_MAC)
                is OS.Desktop.Windows -> Os.isFamily(Os.FAMILY_WINDOWS)
                is OS.Desktop.Linux -> Os.isFamily(Os.FAMILY_UNIX) && !Os.isFamily(Os.FAMILY_MAC)
                else -> false
            }
        }
    }

    open class MacOS(
        override val format: Format.MacOS
    ) : Desktop() {
        override val system = OS.Desktop.MacOS

        object Dmg : MacOS(Format.MacOS.Dmg)
    }

    open class Windows(
        override val format: Format.Windows
    ) : Desktop() {
        override val system = OS.Desktop.Windows

        object Msi : Windows(Format.Windows.Msi)
        object Exe : Windows(Format.Windows.Exe)
        object Portable : Windows(Format.Windows.Zip)
    }

    open class Linux(
        override val format: Format.Linux
    ) : Desktop() {
        override val system = OS.Desktop.Linux

        object Deb : Linux(Format.Linux.Deb)
        object Rpm : Linux(Format.Linux.Rpm)
        object AppImage : Linux(Format.Linux.AppImage)
    }

    sealed class Web : Target() {
        abstract val system: OS.Web
        override val format: Format = Format.Zip
        override val artifactSuffix get() = system.name
    }

    object Js : Web() {
        override val system: OS.Web = OS.Web.Js
    }

    object WasmJS : Web() {
        override val system: OS.Web = OS.Web.WasmJs
    }

    object Server : Target() {
        override val format = Format.Jar
        override val artifactSuffix = ""
    }
}