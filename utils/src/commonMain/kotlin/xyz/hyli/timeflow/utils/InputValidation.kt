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
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
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
     * Validates email format and length
     * @return error message if invalid, null if valid
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be blank"
            email.length > MAX_EMAIL_LENGTH -> "Email is too long (max $MAX_EMAIL_LENGTH characters)"
            !EMAIL_REGEX.matches(email) -> "Invalid email format"
            else -> null
        }
    }

    /**
     * Validates username (nickname-style, accepts any characters)
     * @return error message if invalid, null if valid
     */
    fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Username cannot be blank"
            username.length > MAX_USERNAME_LENGTH -> "Username is too long (max $MAX_USERNAME_LENGTH characters)"
            else -> null
        }
    }

    /**
     * Validates password strength
     * @return error message if invalid, null if valid
     */
    fun validatePassword(password: String): String? {
        return when {
            password.length < MIN_PASSWORD_LENGTH -> "Password is too short (min $MIN_PASSWORD_LENGTH characters)"
            password.length > MAX_PASSWORD_LENGTH -> "Password is too long (max $MAX_PASSWORD_LENGTH characters)"
            else -> null
        }
    }

    /**
     * Validates schedule/course name
     * @return error message if invalid, null if valid
     */
    fun validateName(name: String, fieldName: String = "Name"): String? {
        return when {
            name.isBlank() -> "$fieldName cannot be blank"
            name.length > MAX_NAME_LENGTH -> "$fieldName is too long (max $MAX_NAME_LENGTH characters)"
            else -> null
        }
    }

    /**
     * Validates note/description text
     * @return error message if invalid, null if valid
     */
    fun validateNote(note: String): String? {
        return when {
            note.length > MAX_NOTE_LENGTH -> "Note is too long (max $MAX_NOTE_LENGTH characters)"
            else -> null
        }
    }

    /**
     * Validates teacher name
     * @return error message if invalid, null if valid
     */
    fun validateTeacher(teacher: String): String? {
        return when {
            teacher.length > MAX_TEACHER_LENGTH -> "Teacher name is too long (max $MAX_TEACHER_LENGTH characters)"
            else -> null
        }
    }

    /**
     * Validates classroom name
     * @return error message if invalid, null if valid
     */
    fun validateClassroom(classroom: String): String? {
        return when {
            classroom.length > MAX_CLASSROOM_LENGTH -> "Classroom name is too long (max $MAX_CLASSROOM_LENGTH characters)"
            else -> null
        }
    }

    // Truncation functions - automatically cut strings to maximum allowed length

    /**
     * Truncates email to maximum allowed length
     */
    fun String.truncateEmail(): String = take(MAX_EMAIL_LENGTH)

    /**
     * Truncates username to maximum allowed length
     */
    fun String.truncateUsername(): String = take(MAX_USERNAME_LENGTH)

    /**
     * Truncates name to maximum allowed length
     */
    fun String.truncateName(): String = take(MAX_NAME_LENGTH)

    /**
     * Truncates teacher name to maximum allowed length
     */
    fun String.truncateTeacher(): String = take(MAX_TEACHER_LENGTH)

    /**
     * Truncates classroom name to maximum allowed length
     */
    fun String.truncateClassroom(): String = take(MAX_CLASSROOM_LENGTH)

    /**
     * Truncates note to maximum allowed length
     */
    fun String.truncateNote(): String = take(MAX_NOTE_LENGTH)
}
