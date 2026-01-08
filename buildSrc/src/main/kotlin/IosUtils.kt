/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

import org.gradle.api.Project

fun Project.ipaArguments(
    destination: String = "generic/platform=iOS",
    sdk: String = "iphoneos",
): Array<String> {
    return arrayOf(
        "xcodebuild",
        "-project", rootDir.resolve("iosApp/iosApp.xcodeproj").absolutePath,
        "-scheme", "iosApp",
        "-destination", destination,
        "-sdk", sdk,
        "CODE_SIGNING_ALLOWED=NO",
        "CODE_SIGNING_REQUIRED=NO",
    )
}