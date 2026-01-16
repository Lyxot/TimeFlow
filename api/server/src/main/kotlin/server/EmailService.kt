/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.server

import io.ktor.server.config.*
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class EmailService(private val config: ApplicationConfig) {
    private val testing = config.propertyOrNull("testing")?.getString()?.toBoolean() ?: false
    val codeExpirationMinutes = config.propertyOrNull("email.codeExpirationMinutes")?.getString()?.toInt() ?: 10

    private val session: Session by lazy {
        if (testing) {
            Session.getInstance(Properties())
        } else {
            val host = config.property("email.host").getString()
            val port = config.property("email.port").getString()
            val username = config.property("email.username").getString()
            val password = config.property("email.password").getString()
            val ssl = config.property("email.ssl").getString().toBoolean()

            val props = Properties().apply {
                put("mail.smtp.host", host)
                put("mail.smtp.port", port)
                put("mail.smtp.auth", "true")
                if (ssl) {
                    put("mail.smtp.ssl.enable", "true")
                } else {
                    put("mail.smtp.starttls.enable", "true")
                }
            }

            Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
        }
    }

    /**
     * Sends a verification code email to the specified address.
     * @param to The recipient email address.
     * @param code The 6-digit verification code.
     */
    suspend fun sendVerificationCode(to: String, code: String) = withContext(Dispatchers.IO) {
        if (testing) {
            println("Testing mode: Skipping sending email to $to with code $code")
            return@withContext
        }
        val from = config.property("email.from").getString()
        val fromAddress = InternetAddress(from, "TimeFlow")
        val message = MimeMessage(session).apply {
            setFrom(fromAddress)
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            subject = "TimeFlow 邮箱验证码"
            setContent(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #333;">TimeFlow 邮箱验证码</h2>
                    <p>您的验证码是：</p>
                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #333;">$code</span>
                    </div>
                    <p>此验证码将在${codeExpirationMinutes}分钟后过期。</p>
                    <p style="color: #666; font-size: 14px;">如果您没有请求此验证码，请忽略此邮件。</p>
                </body>
                </html>
                """.trimIndent(),
                "text/html; charset=UTF-8"
            )
        }

        Transport.send(message)
    }

    /**
     * Generates a random 6-digit verification code.
     */
    fun generateCode(): String {
        return if (testing) TESTING_VERIFICATION_CODE
        else (100000..999999).random().toString()
    }

    companion object {
        const val TESTING_VERIFICATION_CODE = "123456"
    }
}
