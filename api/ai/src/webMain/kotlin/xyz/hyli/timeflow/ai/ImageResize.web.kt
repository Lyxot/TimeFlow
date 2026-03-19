/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

// TODO: Browser Canvas API for image resizing is asynchronous, which does not fit
//  the synchronous `expect fun` signature. Web clients should resize images via
//  Canvas/OffscreenCanvas before encoding to base64 and passing to ScheduleExtractor.
actual fun resizeImage(imageBytes: ByteArray, maxSizeBytes: Long, maxResolution: Int): ResizedImage {
    return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)
}
