/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * 检查系统环境中是否存在指定的命令
 */
fun isCommandAvailable(cmd: String): Boolean {
    val checkCmd = if (Os.isFamily(Os.FAMILY_WINDOWS)) "where" else "which"
    return try {
        val process = ProcessBuilder(checkCmd, cmd).start()
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}

/**
 * 获取可用的 7-Zip 命令名称
 */
fun get7zipCommand(): String? {
    return when {
        isCommandAvailable("7zz") -> "7zz"
        isCommandAvailable("7z") -> "7z"
        else -> null
    }
}

@CacheableTask
abstract class BuildArchiveTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val sourceDir: DirectoryProperty

    @get:Internal
    abstract val prepareDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val archiveFolderName: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun buildArchive() {
        val sourceDirectory = sourceDir.get().asFile
        if (!sourceDirectory.exists()) {
            throw GradleException("输入目录不存在: ${sourceDirectory.absolutePath}")
        }
        if (sourceDirectory.listFiles()?.isEmpty() == true) {
            return
        }

        val folderName = archiveFolderName.orNull ?: sourceDirectory.name
        val prepareDirectory = prepareDir.get().asFile.resolve(folderName)

        // Clean and prepare directory
        prepareDirectory.deleteRecursively()
        prepareDirectory.mkdirs()

        // Copy source to prepare directory
        fileSystemOperations.copy {
            from(sourceDirectory)
            into(prepareDirectory)
        }

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        val sevenZipCmd = get7zipCommand()
        if (sevenZipCmd == null) {
            logger.lifecycle("[Archive] 7z/7zz not found, falling back to 'zip' command.")
        }

        execOperations.exec {
            workingDir(prepareDirectory.parentFile)
            sevenZipCmd?.let {
                commandLine(
                    it,
                    "a",
                    "-tzip",
                    "-mx=9",
                    "-r",
                    "-snl",
                    output.absolutePath,
                    prepareDirectory.name
                )
            }
                ?: commandLine("zip", "-r", "-y", "-9", output.absolutePath, prepareDirectory.name)
        }

        // Clean up prepare directory
        prepareDirectory.deleteRecursively()

        logger.lifecycle("[Archive] Created archive at: ${output.absolutePath}")
    }
}
