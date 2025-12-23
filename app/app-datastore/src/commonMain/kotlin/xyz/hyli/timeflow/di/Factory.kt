/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.di

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.resolve
import xyz.hyli.timeflow.datastore.SettingsDataStore
import xyz.hyli.timeflow.datastore.dataStoreFileName

class Factory(
    private val path: String? = null
) {
    fun createDataStore(): SettingsDataStore =
        SettingsDataStore {
            if (path.isNullOrEmpty()) {
                FileKit.filesDir.resolve(dataStoreFileName).path
            } else {
                path
            }
        }
}