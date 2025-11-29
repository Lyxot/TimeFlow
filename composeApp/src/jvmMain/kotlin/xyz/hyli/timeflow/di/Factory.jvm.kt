/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.di

import net.harawata.appdirs.AppDirsFactory
import xyz.hyli.timeflow.BuildConfig
import xyz.hyli.timeflow.datastore.SettingsDataStore
import xyz.hyli.timeflow.datastore.dataStoreFileName

actual class Factory {
    actual fun createDataStore(): SettingsDataStore =
        SettingsDataStore {
            "${fileDirectory()}/$dataStoreFileName"
        }

    private fun fileDirectory(): String =
        AppDirsFactory.getInstance().getUserDataDir(BuildConfig.APP_NAME, null, BuildConfig.AUTHOR)
}