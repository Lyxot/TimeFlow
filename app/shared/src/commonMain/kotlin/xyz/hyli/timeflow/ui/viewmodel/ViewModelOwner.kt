/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import xyz.hyli.timeflow.di.IAppContainer

class ViewModelOwner(
    appContainer: IAppContainer
): ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    val timeFlowViewModel: TimeFlowViewModel = ViewModelProvider.create(
        owner = this as ViewModelStoreOwner,
        factory = TimeFlowViewModel.Factory,
        extras = TimeFlowViewModel.newCreationExtras(appContainer),
    )[TimeFlowViewModel::class]

    @Suppress("unused")
    fun clear() {
        viewModelStore.clear()
    }

}