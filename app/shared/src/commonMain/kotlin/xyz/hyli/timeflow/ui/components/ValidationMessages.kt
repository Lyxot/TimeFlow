/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.ui.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import xyz.hyli.timeflow.shared.generated.resources.*
import xyz.hyli.timeflow.utils.InputValidation

@Composable
fun localizedValidationMessages() = InputValidation.Messages(
    emailBlank = stringResource(Res.string.validation_email_blank),
    emailTooLong = stringResource(Res.string.validation_email_too_long, InputValidation.MAX_EMAIL_LENGTH),
    emailInvalidFormat = stringResource(Res.string.validation_email_invalid_format),
    usernameBlank = stringResource(Res.string.validation_username_blank),
    usernameTooLong = stringResource(Res.string.validation_username_too_long, InputValidation.MAX_USERNAME_LENGTH),
    passwordTooShort = stringResource(Res.string.validation_password_too_short, InputValidation.MIN_PASSWORD_LENGTH),
    passwordTooLong = stringResource(Res.string.validation_password_too_long, InputValidation.MAX_PASSWORD_LENGTH),
    nameBlank = stringResource(Res.string.validation_name_blank),
    nameTooLong = stringResource(Res.string.validation_name_too_long, InputValidation.MAX_NAME_LENGTH),
    noteTooLong = stringResource(Res.string.validation_note_too_long, InputValidation.MAX_NOTE_LENGTH),
    teacherTooLong = stringResource(Res.string.validation_teacher_too_long, InputValidation.MAX_TEACHER_LENGTH),
    classroomTooLong = stringResource(Res.string.validation_classroom_too_long, InputValidation.MAX_CLASSROOM_LENGTH),
)
