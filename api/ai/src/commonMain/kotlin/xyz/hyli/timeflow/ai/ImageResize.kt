/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ai

/**
 * Result of an image resize operation.
 * @property data The (possibly resized) image bytes.
 * @property format The image format of the output ("jpeg" if resized, original format otherwise).
 * @property wasResized Whether the image was actually resized.
 */
data class ResizedImage(
    val data: ByteArray,
    val format: String,
    val wasResized: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResizedImage) return false
        return data.contentEquals(other.data) && format == other.format && wasResized == other.wasResized
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + wasResized.hashCode()
        return result
    }
}

/**
 * Resize an image if it exceeds the byte size limit or max resolution.
 * Returns the original bytes with wasResized=false if no resize is needed.
 *
 * @param imageBytes Raw image bytes.
 * @param maxSizeBytes Maximum file size in bytes. 0 or negative to skip size check.
 * @param maxResolution Maximum resolution (longest edge in pixels). 0 or negative to skip resolution check.
 * @return [ResizedImage] with the result.
 */
expect fun resizeImage(imageBytes: ByteArray, maxSizeBytes: Long, maxResolution: Int): ResizedImage

/**
 * Detect image format from the first bytes of base64-encoded data.
 */
fun detectImageFormatFromBase64(base64: String): String = when {
    base64.startsWith("/9j/") -> "jpeg"
    base64.startsWith("iVBORw0KGgo") -> "png"
    base64.startsWith("R0lGOD") -> "gif"
    base64.startsWith("UklGR") -> "webp"
    else -> "png"
}

/**
 * Detect image format from raw bytes (magic bytes).
 */
fun detectImageFormat(bytes: ByteArray): String {
    if (bytes.size < 4) return "png"
    return when {
        bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "jpeg"
        bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() -> "png"
        bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() -> "gif"
        bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() -> "webp"
        else -> "png"
    }
}
