/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

import xyz.hyli.timeflow.api.models.detectImageFormat

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.sqrt

actual fun resizeImage(imageBytes: ByteArray, maxSizeBytes: Long, maxResolution: Int): ResizedImage {
    val originalImage = ImageIO.read(ByteArrayInputStream(imageBytes))
        ?: return ResizedImage(imageBytes, "png", false)

    val w = originalImage.width
    val h = originalImage.height
    val maxDim = maxOf(w, h)

    val resolutionScale = if (maxResolution > 0 && maxDim > maxResolution) maxResolution.toDouble() / maxDim else 1.0
    val sizeScale = if (maxSizeBytes > 0 && imageBytes.size > maxSizeBytes) {
        sqrt(maxSizeBytes.toDouble() / imageBytes.size)
    } else 1.0

    val scale = minOf(resolutionScale, sizeScale)
    if (scale >= 1.0) return ResizedImage(imageBytes, detectImageFormat(imageBytes), false)

    val newWidth = (w * scale).toInt().coerceAtLeast(1)
    val newHeight = (h * scale).toInt().coerceAtLeast(1)

    val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
    val g2d = resized.createGraphics()
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null)
    g2d.dispose()

    val output = ByteArrayOutputStream()
    ImageIO.write(resized, "jpeg", output)
    return ResizedImage(output.toByteArray(), "jpeg", true)
}
