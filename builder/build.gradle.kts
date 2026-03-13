/*
 * Copyright (c) 2025-2026 Lyxot and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证。
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/Lyxot/TimeFlow/blob/master/LICENSE
 */

import org.apache.tools.ant.taskdefs.condition.Os
import xyz.hyli.timeflow.buildsrc.BuildAppImageTask
import xyz.hyli.timeflow.buildsrc.BuildArchiveTask
import xyz.hyli.timeflow.buildsrc.BuildType
import xyz.hyli.timeflow.buildsrc.Target
import xyz.hyli.timeflow.buildsrc.capitalize
import kotlin.collections.forEach

Target.appVersion = app.versions.name.get()

// Android Apk Builder
BuildType.all.forEach { buildType ->
    val target = Target.Android
    val projectName = ":app:android"
    val capitalizedType = buildType.capitalized
    tasks.register("buildAndroid${capitalizedType}Apk", Copy::class) {
        description = "Builds the Android $buildType APK."
        group = "build"
        from(project(projectName).layout.buildDirectory.dir("outputs/apk/${buildType}/${target.artifactName}"))
        into(layout.buildDirectory.dir("artifacts/${buildType}/android"))
        dependsOn("${projectName}:assemble${capitalizedType}")
    }
}

// iOS Ipa Builder
BuildType.all.forEach { buildType ->
    val target = Target.Ios
    val projectName = ":app:ios"
    val capitalizedType = buildType.capitalized
    tasks.register("buildIos${capitalizedType}Ipa", Copy::class) {
        description = "Builds the iOS $buildType IPA."
        group = "build"
        onlyIf { Os.isFamily(Os.FAMILY_MAC) }
        from(project(projectName).layout.buildDirectory.dir("archives/${buildType}/${target.artifactName}"))
        into(layout.buildDirectory.dir("artifacts/${buildType}/ios"))
        dependsOn("${projectName}:build${capitalizedType}Ipa")
    }
}

// Desktop native package builder (DMG, MSI, EXE, DEB, RPM)
listOf(
    Target.MacOS.Dmg,
    Target.Windows.Exe,
    Target.Windows.Msi,
    Target.Linux.Deb,
    Target.Linux.Rpm,
).forEach { target ->
    val capitalizedName = target.system.name.capitalize()
    val desktopProject = project(":app:desktop")

    BuildType.all.forEach { buildType ->
        val capitalizedType = buildType.capitalized
        tasks.register("build${capitalizedName}${capitalizedType}${target.format.suffix.capitalize()}", Copy::class) {
            description = "Builds the ${target.system.name} $buildType ${target.format.suffix.uppercase()}."
            group = "build"
            onlyIf { target.matchCurrentSystem() }

            val outputDir = desktopProject.layout.buildDirectory.dir(
                "compose/binaries/main" +
                        (if (buildType.isDebug()) "" else "-release") +
                        "/" + if (target == Target.Windows.Portable) "app" else target.format.suffix
            )
            from(outputDir) {
                include("*${target.format.suffix}")
                rename { target.artifactName }
            }
            into(layout.buildDirectory.dir("artifacts/${buildType}/${target.system.name}-${target.archString}"))
            val dependencyTask = ":app:desktop:package" +
                    (if (buildType.isDebug()) "" else "Release") +
                    if (target == Target.Windows.Portable) "Portable" else target.format.suffix.capitalize()
            dependsOn(dependencyTask)
        }
    }
}

// Windows Portable ZIP Builder
run {
    val target = Target.Windows.Portable
    val desktopProject = project(":app:desktop")
    // Windows Portable packaging tasks
    BuildType.all.forEach { buildType ->
        val capitalizedType = buildType.capitalized
        val binaryPath =
            "compose/binaries/main" + (if (buildType.isRelease()) "-release" else "") + "/app/${Target.APP_NAME}"

        tasks.register("buildWindows${capitalizedType}Portable", BuildArchiveTask::class) {
            group = "build"
            description = "Packages the application as a Windows Portable $buildType ZIP"
            onlyIf { target.matchCurrentSystem() }

            sourceDir.set(desktopProject.layout.buildDirectory.dir(binaryPath))
            prepareDir.set(layout.buildDirectory.dir("tmp/${target.system.name}/${buildType}"))
            outputFile.set(layout.buildDirectory.file("artifacts/${buildType}/${target.system.name}-${target.archString}/${target.artifactName}"))

            val dependencyTask = ":app:desktop:create" +
                    (if (buildType.isRelease()) "Release" else "") +
                    "PortableDistributable"
            dependsOn(dependencyTask)
        }
    }
}

// Linux AppImage Builder
run {
    val target = Target.Linux.AppImage
    val desktopProject = project(":app:desktop")
    BuildType.all.forEach { buildType ->
        val capitalizedType = buildType.capitalized
        tasks.register("buildLinux${capitalizedType}AppImage", BuildAppImageTask::class) {
            description = "Builds the Linux $buildType AppImage."
            group = "build"
            onlyIf { target.matchCurrentSystem() }

            val binaryPath = "compose/binaries/main" + (if (buildType.isDebug()) "" else "-release") + "/app/TimeFlow"
            distributableDir.set(desktopProject.layout.buildDirectory.dir(binaryPath))
            iconFile.set(desktopProject.file("desktopAppIcons/LinuxIcon.png"))
            desktopFile.set(file("resources/appimage/TimeFlow.desktop"))
            appRunFile.set(file("resources/appimage/AppRun"))
            appImageToolDir.set(layout.buildDirectory.dir("appimagetool"))
            outputFile.set(layout.buildDirectory.file("artifacts/${buildType}/linux-${target.archString}/${target.artifactName}"))
            arch.set(target.archString)

            val dependencyTask = ":app:desktop:create" +
                    (if (buildType.isRelease()) "Release" else "") +
                    "Distributable"
            dependsOn(dependencyTask)
        }
    }
}

// Web Archive Builder
listOf(
    Target.WasmJS, Target.Js
).forEach { target ->
    val projectName = ":app:web"
    val webProject = project(projectName)
    val capitalizedName = target.system.name.capitalize()
    BuildType.all.forEach { buildType ->
        val capitalizedType = buildType.capitalized
        val executableType = if (buildType.isDebug()) "developmentExecutable" else "productionExecutable"
        val distributionTask = if (buildType.isDebug()) "DevelopmentExecutableDistribution" else "Distribution"
        tasks.register("build${capitalizedName}${capitalizedType}Zip", BuildArchiveTask::class) {
            description = "Builds the ${target.system.name} $buildType archive."
            group = "build"

            sourceDir.set(webProject.layout.buildDirectory.dir("dist/${target.system.name}/${executableType}"))
            prepareDir.set(layout.buildDirectory.dir("tmp/${target.system.name}/${buildType}"))
            archiveFolderName.set(Target.APP_NAME)
            outputFile.set(layout.buildDirectory.file("artifacts/${buildType}/${target.system.name}/${target.artifactName}"))
            dependsOn("${projectName}:${target.system.name}Browser${distributionTask}")
        }
    }
}

// Web Compatibility Archive Builder (combines wasmJs and js with automatic fallback)
run {
    val target = Target.WebCompat
    val projectName = ":app:web"
    val webProject = project(projectName)
    val capitalizedName = target.system.name.capitalize()

    // Only support release build as composeCompatibilityBrowserDistribution only builds release
    val buildType = BuildType.Release
    val capitalizedType = buildType.capitalized

    tasks.register("build${capitalizedName}${capitalizedType}Zip", BuildArchiveTask::class) {
        description = "Builds the ${target.system.name} $buildType archive with automatic wasm/js fallback."
        group = "build"

        sourceDir.set(webProject.layout.buildDirectory.dir("dist/composeWebCompatibility/productionExecutable"))
        prepareDir.set(layout.buildDirectory.dir("tmp/${target.system.name}/${buildType}"))
        archiveFolderName.set(Target.APP_NAME)
        outputFile.set(layout.buildDirectory.file("artifacts/${buildType}/${target.system.name}/${target.artifactName}"))
        dependsOn("${projectName}:composeCompatibilityBrowserDistribution")
    }
}
