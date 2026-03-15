/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.data

private val PNG_SIGNATURE = byteArrayOf(
    0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
)
private val IEND_CHUNK = byteArrayOf(
    0x00, 0x00, 0x00, 0x00,
    0x49, 0x45, 0x4E, 0x44,
    0xAE.toByte(), 0x42, 0x60, 0x82.toByte()
)

/**
 * Find the byte offset immediately after the IEND chunk in PNG data.
 * Returns -1 if the data is not a valid PNG or IEND is not found.
 */
fun findPngEnd(bytes: ByteArray): Int {
    if (bytes.size < PNG_SIGNATURE.size || !bytes.startsWith(PNG_SIGNATURE)) return -1
    val idx = bytes.indexOf(IEND_CHUNK)
    if (idx == -1) return -1
    return idx + IEND_CHUNK.size
}

/**
 * Extract the payload appended after a PNG's IEND chunk.
 * Returns null if the data is not a PNG or has no appended payload.
 */
fun extractPngPayload(bytes: ByteArray): ByteArray? {
    val end = findPngEnd(bytes)
    if (end == -1 || end >= bytes.size) return null
    return bytes.copyOfRange(end, bytes.size)
}

private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (size < prefix.size) return false
    for (i in prefix.indices) {
        if (this[i] != prefix[i]) return false
    }
    return true
}

private fun ByteArray.indexOf(pattern: ByteArray): Int {
    if (pattern.isEmpty()) return 0
    outer@ for (i in 0..size - pattern.size) {
        for (j in pattern.indices) {
            if (this[i + j] != pattern[j]) continue@outer
        }
        return i
    }
    return -1
}

fun readSettingsFromByteArray(bytes: ByteArray): Settings? {
    return bytes.toProtoBufData<SettingsV2>(null)
        ?: bytes.toProtoBufData<SettingsV1>(null)?.toV2()
}

fun readScheduleFromByteArray(bytes: ByteArray): Schedule? {
    return bytes.toProtoBufData<ScheduleV2>(null)
        ?: bytes.toProtoBufData<ScheduleV1>(null)?.toV2()
}

fun readScheduleFromPng(bytes: ByteArray): Schedule? {
    val payload = extractPngPayload(bytes) ?: return null
    return readScheduleFromByteArray(payload)
}