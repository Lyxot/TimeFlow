/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import xyz.hyli.timeflow.buildsrc.BuildArchiveTask
import xyz.hyli.timeflow.buildsrc.Target
import xyz.hyli.timeflow.buildsrc.capitalize
import xyz.hyli.timeflow.buildsrc.ipaArguments

/**
 * Packages an unsigned IPA **and** injects an ad‑hoc signature so sideloaders can re‑sign it.
 *
 * This task is **configuration‑cache safe** – it does *not* capture the `Project` instance.
 */
/**
 * Packages an unsigned IPA **and** injects an ad‑hoc signature so sideloaders can re‑sign it.
 *
 * This task is **configuration‑cache safe** – it does *not* capture the `Project` instance.
 */
@CacheableTask
abstract class BuildIpaPayloadTask : DefaultTask() {

    /* -------------------------------------------------------------
     * Inputs / outputs
     * ----------------------------------------------------------- */

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val archiveDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIpaPayloadDir: RegularFileProperty

    /* -------------------------------------------------------------
     * Services (injected)
     * ----------------------------------------------------------- */

    @get:Inject
    abstract val execOperations: ExecOperations

    /* -------------------------------------------------------------
     * Action
     * ----------------------------------------------------------- */

    @TaskAction
    fun buildIpaPayload() {
        // 1. Locate the .app inside the .xcarchive
        val appDir = archiveDir.get().asFile.resolve("Products/Applications/TimeFlow.app")
        if (!appDir.exists())
            throw GradleException("Could not find TimeFlow.app in archive at: ${appDir.absolutePath}")

        // 2. Create temporary Payload directory and copy .app into it
        val payloadDir = outputIpaPayloadDir.get().asFile.apply { mkdirs() }
        val destApp = File(payloadDir, appDir.name)
        appDir.copyRecursively(destApp, overwrite = true)

        // 3. Inject placeholder (ad‑hoc) code signature so AltStore / SideStore accept it
        logger.lifecycle("[IPA] Ad‑hoc signing ${destApp.name} …")
        execOperations.exec {
            commandLine(
                "codesign", "--force", "--deep", "--sign", "-", "--timestamp=none",
                destApp.absolutePath,
            )
        }
    }
}

Target.appVersion = app.versions.name.get()

listOf(
    "debug", "release"
).forEach { buildType ->
    val capitalizedName = buildType.capitalize()

    val buildArchive = tasks.register("build${capitalizedName}Archive", Exec::class) {
        group = "build"
        description = "Builds the iOS framework for ${capitalizedName}"
        workingDir(projectDir)

        val output = layout.buildDirectory.dir("archives/${buildType}/TimeFlow.xcarchive")
        outputs.dir(output)
        commandLine(
            *ipaArguments(),
            "archive",
            "-configuration", capitalizedName,
            "-archivePath", output.get().asFile.absolutePath,
        )
    }
    val buildPayload = tasks.register("build${capitalizedName}IpaPayload", BuildIpaPayloadTask::class) {
        description = "Manually packages the .app from the .xcarchive into an unsigned .ipa payload"
        group = "build"

        // Adjust these paths as needed
        archiveDir = layout.buildDirectory.dir("archives/${buildType}/TimeFlow.xcarchive")
        outputIpaPayloadDir = layout.buildDirectory.file("archives/${buildType}/Payload")
        dependsOn(buildArchive)
    }
    tasks.register("build${capitalizedName}Ipa", BuildArchiveTask::class) {
        description = "Manually packages the .app from the .xcarchive into an unsigned .ipa"
        group = "build"

        sourceDir.set(layout.buildDirectory.dir("archives/${buildType}/Payload"))
        prepareDir.set(layout.buildDirectory.dir("archives/${buildType}/tmp"))
        archiveFolderName.set("Payload")
        outputFile.set(layout.buildDirectory.file("archives/${buildType}/${Target.Ios.artifactName}"))
        dependsOn(buildPayload)
    }
}