/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import xyz.hyli.timeflow.api.models.detectImageFormat
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

actual fun resizeImage(imageBytes: ByteArray, maxSizeBytes: Long, maxResolution: Int): ResizedImage {
    val original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        ?: return ResizedImage(imageBytes, "png", false)

    val w = original.width
    val h = original.height
    val maxDim = maxOf(w, h)

    val resolutionScale = if (maxResolution > 0 && maxDim > maxResolution) maxResolution.toDouble() / maxDim else 1.0
    val sizeScale = if (maxSizeBytes > 0 && imageBytes.size > maxSizeBytes) {
        sqrt(maxSizeBytes.toDouble() / imageBytes.size)
    } else 1.0

    val scale = minOf(resolutionScale, sizeScale)
    if (scale >= 1.0) {
        original.recycle()
        return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)
    }

    val newWidth = (w * scale).toInt().coerceAtLeast(1)
    val newHeight = (h * scale).toInt().coerceAtLeast(1)

    val resized = Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    original.recycle()

    val output = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
    resized.recycle()

    return ResizedImage(output.toByteArray(), "jpeg", true)
}
