/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import xyz.hyli.timeflow.api.models.detectImageFormat
import kotlin.math.sqrt

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
actual fun resizeImage(imageBytes: ByteArray, maxSizeBytes: Long, maxResolution: Int): ResizedImage {
    val nsData = imageBytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = imageBytes.size.toULong())
    }
    val uiImage = UIImage.imageWithData(nsData)
        ?: return ResizedImage(imageBytes, "png", false)

    val cgImage = uiImage.CGImage ?: return ResizedImage(imageBytes, "png", false)
    val w = CGImageGetWidth(cgImage).toInt()
    val h = CGImageGetHeight(cgImage).toInt()
    val maxDim = maxOf(w, h)

    val resolutionScale = if (maxResolution > 0 && maxDim > maxResolution) maxResolution.toDouble() / maxDim else 1.0
    val sizeScale = if (maxSizeBytes > 0 && imageBytes.size > maxSizeBytes) {
        sqrt(maxSizeBytes.toDouble() / imageBytes.size)
    } else 1.0

    val scale = minOf(resolutionScale, sizeScale)
    if (scale >= 1.0) return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)

    val newWidth = (w * scale).toInt().coerceAtLeast(1)
    val newHeight = (h * scale).toInt().coerceAtLeast(1)

    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val context = CGBitmapContextCreate(
        data = null,
        width = newWidth.toULong(),
        height = newHeight.toULong(),
        bitsPerComponent = 8u,
        bytesPerRow = (newWidth * 4).toULong(),
        space = colorSpace,
        bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value
    ) ?: return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)

    CGContextDrawImage(context, CGRectMake(0.0, 0.0, newWidth.toDouble(), newHeight.toDouble()), cgImage)

    val resizedCGImage = CGBitmapContextCreateImage(context)
        ?: return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)

    val resizedUIImage = UIImage.imageWithCGImage(resizedCGImage)
    val jpegData = UIImageJPEGRepresentation(resizedUIImage, 0.85)
        ?: return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)

    val resultBytes = ByteArray(jpegData.length.toInt())
    resultBytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), jpegData.bytes, jpegData.length)
    }

    return ResizedImage(resultBytes, "jpeg", true)
}
