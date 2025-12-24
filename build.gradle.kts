/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import java.lang.System.getenv
import java.net.HttpURLConnection
import java.net.URI

/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.multiplatform.android).apply(false)
    alias(libs.plugins.aboutLibraries).apply(false)
    alias(libs.plugins.build.config).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.hot.reload).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.ktor).apply(false)
}

val appVersionCode = app.versions.major.get().toInt() * 10000 +
        try {
            val githubToken = getenv("GITHUB_TOKEN")
            val url =
                URI("https://api.github.com/repos/Lyxot/TimeFlow/commits?sha=master&per_page=1").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            if (githubToken != null) {
                connection.setRequestProperty("Authorization", "Bearer $githubToken")
            }
            val linkHeader = connection.getHeaderField("Link") ?: ""
            val lastPagePattern = ".*page=(\\d+)>; rel=\"last\".*".toRegex()
            val match = lastPagePattern.find(linkHeader)
            match?.groupValues?.get(1)?.toInt() ?: 0
        } catch (e: Exception) {
            println("Error getting commit count from GitHub API: ${e.message}")
            0
        }

val versionName: Any = app.versions.major.get()

ext {
    set("appVersionCode", appVersionCode)
}

allprojects {
    group = "xyz.hyli.timeflow"
    version = versionName
}