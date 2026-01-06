/*
 * Copyright (c) 2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

package xyz.hyli.timeflow.buildsrc

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File
import java.net.URI
import javax.inject.Inject

@CacheableTask
abstract class BuildAppImageTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val distributableDir: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iconFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val desktopFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val appRunFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Internal
    abstract val appImageToolDir: DirectoryProperty

    @get:Input
    abstract val arch: Property<String>

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun buildAppImage() {
        val archValue = arch.get()
        val appImageToolName = "appimagetool-$archValue.AppImage"
        val appImageToolFile = appImageToolDir.get().file(appImageToolName).asFile
        val appImageToolUrl = "https://github.com/AppImage/appimagetool/releases/download/continuous/$appImageToolName"

        // Download appimagetool if not exists
        if (!appImageToolFile.exists()) {
            appImageToolFile.parentFile.mkdirs()
            logger.lifecycle("[AppImage] Downloading appimagetool from $appImageToolUrl")
            URI(appImageToolUrl).toURL().openStream().use { input ->
                appImageToolFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            appImageToolFile.setExecutable(true)
        }

        val distributable = distributableDir.get().asFile
        val output = outputFile.get().asFile
        val appDir = File(output.parentFile, "AppDir")

        if (!distributable.exists()) {
            throw GradleException("[AppImage] Distributable not found at ${distributable.absolutePath}")
        }

        // Clean and create AppDir
        appDir.deleteRecursively()
        appDir.mkdirs()
        File(appDir, "usr").mkdirs()

        // Copy distributable contents to AppDir/usr
        fileSystemOperations.copy {
            from(distributable)
            into(File(appDir, "usr"))
        }

        // Copy .desktop file
        fileSystemOperations.copy {
            from(desktopFile)
            into(appDir)
        }

        // Copy and set executable AppRun
        val appRunDest = File(appDir, "AppRun")
        fileSystemOperations.copy {
            from(appRunFile)
            into(appDir)
        }
        appRunDest.setExecutable(true)

        // Copy icon
        fileSystemOperations.copy {
            from(iconFile)
            into(appDir)
            rename { "TimeFlow.png" }
        }

        // Set executable permissions on main binary
        val mainBinary = File(appDir, "usr/bin/TimeFlow")
        if (mainBinary.exists()) {
            mainBinary.setExecutable(true)
        }

        // Build AppImage
        logger.lifecycle("[AppImage] Building: ${output.name}")
        execOperations.exec {
            environment("ARCH", archValue)
            workingDir(output.parentFile)
            commandLine(appImageToolFile.absolutePath, appDir.absolutePath)
        }

        // Rename output file (appimagetool outputs as TimeFlow-{arch}.AppImage)
        val generatedAppImage = File(output.parentFile, "TimeFlow-$archValue.AppImage")
        if (generatedAppImage.exists() && generatedAppImage != output) {
            generatedAppImage.renameTo(output)
        }
        output.setExecutable(true)

        // Clean up AppDir after successful build
        appDir.deleteRecursively()

        logger.lifecycle("[AppImage] Created AppImage at: ${output.absolutePath}")
    }
}
