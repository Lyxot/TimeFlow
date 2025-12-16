/*
 * Copyright (c) 2025 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data

import kotlin.random.Random

const val ZERO_ID = 0.toShort()

val random = Random.Default

fun getRandomShort(): Short =
    random.nextInt(1, Short.MAX_VALUE.toInt() + 1).toShort()

fun newShortId(
    idList: Set<Short>
): Short {
    var newId: Short = getRandomShort()
    while (idList.contains(newId)) {
        newId = getRandomShort()
    }
    return newId
}