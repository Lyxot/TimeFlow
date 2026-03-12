/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow

import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import xyz.hyli.timeflow.server.*
import xyz.hyli.timeflow.server.database.DatabaseFactory
import xyz.hyli.timeflow.server.database.ExposedDataRepository
import xyz.hyli.timeflow.server.isTestMode
import java.io.File

fun main(args: Array<String>) {
    val commandLine = CommandLineArgs.parse(args)
    val deploymentConfig = loadConfig(commandLine)
    val ktorDeployment = runCatching { deploymentConfig.config("ktor.deployment") }
        .getOrElse { MapApplicationConfig() }
    val environment = applicationEnvironment {
        config = deploymentConfig
    }

    embeddedServer(
        factory = Netty,
        environment = environment,
        configure = {
            loadCommonConfiguration(ktorDeployment)
            shutdownGracePeriod = 2000
            shutdownTimeout = 5000
            connectors.clear()
            connectors.add(
                EngineConnectorBuilder().apply {
                    host = commandLine.host ?: deploymentConfig.propertyOrNull("host")?.getString() ?: "0.0.0.0"
                    port = commandLine.port ?: deploymentConfig.property("port").getString().toInt()
                }
            )
        },
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val log = LoggerFactory.getLogger("xyz.hyli.timeflow.Application")

    validateConfig(environment.config)
    DatabaseFactory.init(config = environment.config)
    val repository = ExposedDataRepository()
    val accessTokenBlacklist = AccessTokenBlacklist()

    val turnstileService = TurnstileService(environment.config)

    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureAdministration()
    configureSecurity(repository, accessTokenBlacklist)
    configureRouting(repository, accessTokenBlacklist, turnstileService)

    monitor.subscribe(ApplicationStopping) {
        log.info("Server shutting down, closing resources...")
        turnstileService.close()
        DatabaseFactory.close()
        log.info("Resources closed")
    }
}

private fun validateConfig(config: ApplicationConfig) {
    if (isTestMode) return

    val errors = mutableListOf<String>()

    fun requireNonBlank(key: String, description: String) {
        val value = config.propertyOrNull(key)?.getString()
        if (value.isNullOrBlank()) {
            errors += "$description ($key) must be configured"
        }
    }

    // PostgreSQL credentials are always required in production
    requireNonBlank("postgres.user", "Database user")
    requireNonBlank("postgres.password", "Database password")

    // Email credentials are required when email verification is enabled
    val emailEnabled = config.propertyOrNull("email.verificationEnabled")?.getString()?.toBoolean() ?: true
    if (emailEnabled) {
        requireNonBlank("email.host", "Email SMTP host")
        requireNonBlank("email.username", "Email username")
        requireNonBlank("email.password", "Email password")
        requireNonBlank("email.from", "Email from address")
    }

    if (errors.isNotEmpty()) {
        throw IllegalStateException(
            "Server configuration is incomplete:\n" + errors.joinToString("\n") { "  - $it" }
        )
    }
}

private fun loadConfig(commandLine: CommandLineArgs): ApplicationConfig {
    var config: ApplicationConfig = defaultConfig()
    commandLine.configPaths.forEach { path ->
        loadConfigFileOrNull(path)?.let { loadedConfig ->
            config = config.mergeWith(loadedConfig)
        }
    }

    val environmentOverrides = collectEnvironmentOverrides(config)
    if (environmentOverrides.isNotEmpty()) {
        config = config.mergeWith(MapApplicationConfig(environmentOverrides))
    }

    if (commandLine.propertyOverrides.isNotEmpty()) {
        config = config.mergeWith(MapApplicationConfig(commandLine.propertyOverrides))
    }

    return config
}

private fun collectEnvironmentOverrides(config: ApplicationConfig): List<Pair<String, String>> {
    val overrides = mutableListOf<Pair<String, String>>()
    collectConfigKeys(config).forEach { key ->
        System.getenv(toEnvironmentVariableName(key))?.let { overrides += key to it }
        System.getProperty(key)?.let { overrides += key to it }
    }
    return overrides
}

private fun collectConfigKeys(config: ApplicationConfig, prefix: String = ""): Set<String> {
    val keys = mutableSetOf<String>()

    config.keys().forEach { key ->
        val qualifiedKey = if (prefix.isEmpty()) key else "$prefix.$key"
        if (config.propertyOrNull(key) != null) {
            keys += qualifiedKey
        }

        runCatching { config.config(key) }
            .getOrNull()
            ?.let { keys += collectConfigKeys(it, qualifiedKey) }
    }

    return keys
}

private fun toEnvironmentVariableName(key: String): String = buildString {
    append("${BuildConfig.APP_NAME}_")
    val normalizedKey = key.removePrefix("ktor.")
    normalizedKey.forEachIndexed { index, char ->
        when {
            char == '.' -> append('_')
            char.isUpperCase() -> {
                if (index > 0 && normalizedKey[index - 1] != '.') {
                    append('_')
                }
                append(char)
            }

            else -> append(char.uppercaseChar())
        }
    }
}

private fun defaultConfig(): ApplicationConfig = MapApplicationConfig(
    "host" to "0.0.0.0",
    "port" to "8080",
    "jwt.domain" to "http://localhost:8080",
    "jwt.audience" to "timeflow-client",
    "jwt.realm" to "TimeFlow API",
    "jwt.issuer" to "timeflow-api",
    "jwt.privateKeyPath" to "jwt-private.pem",
    "jwt.publicKeyPath" to "jwt-public.pem",
    "postgres.host" to "localhost",
    "postgres.port" to "5432",
    "postgres.database" to "timeflow",
    "postgres.user" to "",
    "postgres.password" to "",
    "postgres.maximumPoolSize" to "10",
    "email.verificationEnabled" to "true",
    "email.host" to "",
    "email.port" to "465",
    "email.username" to "",
    "email.password" to "",
    "email.from" to "",
    "email.ssl" to "true",
    "email.codeExpirationMinutes" to "10",
    "http.forwardedHeaders.enabled" to "true",
    "http.xForwardedHeaders.enabled" to "true",
    "http.cors.enabled" to "false",
    "http.cors.anyHost" to "false",
    "http.cors.allowedHosts" to "",
    "http.cors.allowCredentials" to "false",
    "http.hsts.enabled" to "false",
    "http.hsts.includeSubDomains" to "true",
    "http.hsts.preload" to "false",
    "http.hsts.maxAgeInSeconds" to "31536000",
    "http.httpsRedirect.enabled" to "false",
    "http.httpsRedirect.sslPort" to "443",
    "http.httpsRedirect.permanentRedirect" to "true",
    "webApp.serveEnabled" to BuildConfig.BUNDLED_WEB_APP_ZIP.toString(),
    "webApp.zipPath" to "",
    "turnstile.enabled" to "false",
    "turnstile.secretKey" to "",
    "turnstile.siteVerifyUrl" to "https://challenges.cloudflare.com/turnstile/v0/siteverify"
)

private fun loadConfigFileOrNull(path: String): ApplicationConfig? = try {
    val resolvedPath = resolveConfigPath(path)
    requireNotNull(YamlConfigLoader().load(resolvedPath)) {
        "Config file '$path' could not be loaded"
    }
} catch (_: MissingConfigFileException) {
    null
} catch (e: Exception) {
    throw IllegalStateException("Failed to load config file at '$path'.", e)
}

private fun resolveConfigPath(path: String): String {
    val directFile = File(path)
    if (directFile.isAbsolute || directFile.exists()) {
        return directFile.path
    }

    var current: File? = File(System.getProperty("user.dir")).absoluteFile
    while (current != null) {
        val candidate = current.resolve(path)
        if (candidate.exists()) {
            return candidate.path
        }
        current = current.parentFile
    }

    throw MissingConfigFileException(path)
}

private class MissingConfigFileException(path: String) :
    IllegalArgumentException("Config file '$path' does not exist")

private data class CommandLineArgs(
    val configPaths: List<String>,
    val host: String?,
    val port: Int?,
    val propertyOverrides: List<Pair<String, String>>
) {
    companion object {
        private const val DEFAULT_CONFIG_PATH = "server.yml"

        fun parse(args: Array<String>): CommandLineArgs {
            val configPaths = mutableListOf<String>()
            val propertyOverrides = mutableListOf<Pair<String, String>>()
            var host: String? = null
            var port: Int? = null
            var index = 0

            while (index < args.size) {
                val arg = args[index]
                when {
                    arg.startsWith("-P:") -> {
                        propertyOverrides += splitOption(arg, "-P:")
                    }

                    arg.startsWith("--config=") || arg.startsWith("-c=") -> {
                        configPaths += arg.substringAfter('=')
                    }

                    arg == "--config" || arg == "-c" -> {
                        configPaths += requireValue(args, ++index, arg)
                    }

                    arg.startsWith("--host=") || arg.startsWith("-h=") -> {
                        host = arg.substringAfter('=')
                    }

                    arg == "--host" || arg == "-h" -> {
                        host = requireValue(args, ++index, arg)
                    }

                    arg.startsWith("--port=") || arg.startsWith("-p=") -> {
                        port = arg.substringAfter('=').toInt()
                    }

                    arg == "--port" || arg == "-p" -> {
                        port = requireValue(args, ++index, arg).toInt()
                    }
                }
                index++
            }

            return CommandLineArgs(
                configPaths = configPaths.ifEmpty { listOf(DEFAULT_CONFIG_PATH) },
                host = host,
                port = port,
                propertyOverrides = propertyOverrides
            )
        }

        private fun requireValue(args: Array<String>, index: Int, option: String): String {
            if (index >= args.size) {
                throw IllegalArgumentException("Missing value for $option")
            }
            return args[index]
        }

        private fun splitOption(arg: String, prefix: String): Pair<String, String> {
            val payload = arg.removePrefix(prefix)
            val separatorIndex = payload.indexOf('=')
            require(separatorIndex > 0) { "Invalid option: $arg" }
            return payload.substring(0, separatorIndex) to payload.substring(separatorIndex + 1)
        }
    }
}
