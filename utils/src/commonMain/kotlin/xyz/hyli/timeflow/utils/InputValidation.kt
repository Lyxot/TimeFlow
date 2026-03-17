/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.utils

/**
 * Input validation utilities for API endpoints
 */
object InputValidation {
    // RFC 5322 simplified email regex
    private val EMAIL_REGEX = Regex(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,63}$"
    )

    // User field limits
    const val MAX_EMAIL_LENGTH = 255
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_PASSWORD_LENGTH = 128
    const val MIN_PASSWORD_LENGTH = 8

    // Schedule/Course field limits (matching database varchar/text constraints)
    const val MAX_NAME_LENGTH = 255
    const val MAX_TEACHER_LENGTH = 128
    const val MAX_CLASSROOM_LENGTH = 128
    const val MAX_NOTE_LENGTH = 1000  // Reasonable limit for text field

    /**
     * Returns the number of Unicode code points in the string.
     * Unlike String.length which counts UTF-16 code units,
     * this correctly counts supplementary characters (e.g. emoji) as 1.
     */
    fun String.codePointCount(): Int {
        var count = 0
        var i = 0
        while (i < length) {
            val c = this[i]
            if (c.isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) {
                i += 2
            } else {
                i++
            }
            count++
        }
        return count
    }

    /**
     * Truncates the string to at most [maxCodePoints] Unicode code points,
     * without splitting surrogate pairs.
     */
    fun String.takeCodePoints(maxCodePoints: Int): String {
        var count = 0
        var i = 0
        while (i < length && count < maxCodePoints) {
            val c = this[i]
            if (c.isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) {
                i += 2
            } else {
                i++
            }
            count++
        }
        return substring(0, i)
    }

    /**
     * Localizable validation messages. Default values are English.
     * Pass a custom instance with localized strings from the UI layer.
     */
    data class Messages(
        val emailBlank: String = "Email cannot be blank",
        val emailTooLong: String = "Email is too long (max $MAX_EMAIL_LENGTH characters)",
        val emailInvalidFormat: String = "Invalid email format",
        val usernameBlank: String = "Username cannot be blank",
        val usernameTooLong: String = "Username is too long (max $MAX_USERNAME_LENGTH characters)",
        val passwordTooShort: String = "Password is too short (min $MIN_PASSWORD_LENGTH characters)",
        val passwordTooLong: String = "Password is too long (max $MAX_PASSWORD_LENGTH characters)",
        val nameBlank: String = "Name cannot be blank",
        val nameTooLong: String = "Name is too long (max $MAX_NAME_LENGTH characters)",
        val noteTooLong: String = "Note is too long (max $MAX_NOTE_LENGTH characters)",
        val teacherTooLong: String = "Teacher name is too long (max $MAX_TEACHER_LENGTH characters)",
        val classroomTooLong: String = "Classroom name is too long (max $MAX_CLASSROOM_LENGTH characters)",
    )

    val defaultMessages = Messages()

    /**
     * Validates email format and length
     * @return error message if invalid, null if valid
     */
    fun validateEmail(email: String, messages: Messages = defaultMessages): String? {
        return when {
            email.isBlank() -> messages.emailBlank
            email.codePointCount() > MAX_EMAIL_LENGTH -> messages.emailTooLong
            !EMAIL_REGEX.matches(email) -> messages.emailInvalidFormat
            else -> null
        }
    }

    /**
     * Validates username (nickname-style, accepts any characters)
     * @return error message if invalid, null if valid
     */
    fun validateUsername(username: String, messages: Messages = defaultMessages): String? {
        return when {
            username.isBlank() -> messages.usernameBlank
            username.codePointCount() > MAX_USERNAME_LENGTH -> messages.usernameTooLong
            else -> null
        }
    }

    /**
     * Validates password strength
     * @return error message if invalid, null if valid
     */
    fun validatePassword(password: String, messages: Messages = defaultMessages): String? {
        return when {
            password.length < MIN_PASSWORD_LENGTH -> messages.passwordTooShort
            password.length > MAX_PASSWORD_LENGTH -> messages.passwordTooLong
            else -> null
        }
    }

    /**
     * Validates schedule/course name
     * @return error message if invalid, null if valid
     */
    fun validateName(name: String, messages: Messages = defaultMessages): String? {
        return when {
            name.isBlank() -> messages.nameBlank
            name.codePointCount() > MAX_NAME_LENGTH -> messages.nameTooLong
            else -> null
        }
    }

    /**
     * Validates note/description text
     * @return error message if invalid, null if valid
     */
    fun validateNote(note: String, messages: Messages = defaultMessages): String? {
        return when {
            note.codePointCount() > MAX_NOTE_LENGTH -> messages.noteTooLong
            else -> null
        }
    }

    /**
     * Validates teacher name
     * @return error message if invalid, null if valid
     */
    fun validateTeacher(teacher: String, messages: Messages = defaultMessages): String? {
        return when {
            teacher.codePointCount() > MAX_TEACHER_LENGTH -> messages.teacherTooLong
            else -> null
        }
    }

    /**
     * Validates classroom name
     * @return error message if invalid, null if valid
     */
    fun validateClassroom(classroom: String, messages: Messages = defaultMessages): String? {
        return when {
            classroom.codePointCount() > MAX_CLASSROOM_LENGTH -> messages.classroomTooLong
            else -> null
        }
    }

    // Truncation functions - automatically cut strings to maximum allowed code points

    fun String.truncateEmail(): String = takeCodePoints(MAX_EMAIL_LENGTH)

    fun String.truncateUsername(): String = takeCodePoints(MAX_USERNAME_LENGTH)

    fun String.truncateName(): String = takeCodePoints(MAX_NAME_LENGTH)

    fun String.truncateTeacher(): String = takeCodePoints(MAX_TEACHER_LENGTH)

    fun String.truncateClassroom(): String = takeCodePoints(MAX_CLASSROOM_LENGTH)

    fun String.truncateNote(): String = takeCodePoints(MAX_NOTE_LENGTH)
}
