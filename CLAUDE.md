# CLAUDE.md

This file provides guidance for Claude Code when working with this codebase.

## Project Overview

TimeFlow is a schedule/timetable application built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). It targets Android, iOS, Desktop (Windows, macOS, Linux), and Web (JS, WasmJS) platforms from a single codebase.

## Tech Stack

- **Language**: Kotlin 2.3+
- **UI Framework**: Compose Multiplatform 1.11.0-alpha01
- **Design System**: Material Design 3 Expressive
- **Build System**: Gradle with Kotlin DSL
- **Dependency Injection**: kotlin-inject
- **Serialization**: kotlinx-serialization (protobuf)
- **Data Storage**: androidx-datastore
- **JVM**: JDK 17+ required

## Project Structure

```
TimeFlow/
├── app/
│   ├── android/          # Android app entry point
│   ├── ios/              # iOS app entry point (Kotlin)
│   ├── desktop/          # Desktop (JVM) app entry point
│   ├── web/              # Web (JS/WasmJS) app entry point
│   ├── shared/           # Shared UI and business logic (commonMain)
│   ├── app-datastore/    # DataStore preferences module
│   └── app-interface/    # Shared interfaces
├── data/                 # Data models and repositories
├── utils/                # Utilities and BuildConfig generation
├── builder/              # Unified build tasks for all platforms
├── buildSrc/             # Custom Gradle tasks and build utilities
├── iosApp/               # Xcode project for iOS
├── icon/                 # App icons for all platforms
└── gradle/               # Version catalogs (libs.versions.toml, app.versions.toml)
```

## Build Commands

Build tasks follow the naming convention `build{Target}{BuildType}{Format}`, e.g. `buildAndroidReleaseApk`, `buildLinuxDebugAppImage`.

```bash
# Android
./gradlew buildAndroidReleaseApk

# iOS
./gradlew buildIosReleaseIpa

# macOS
./gradlew buildMacosReleaseDmg

# Linux
./gradlew buildLinuxReleaseAppImage
./gradlew buildLinuxReleaseDeb
./gradlew buildLinuxReleaseRpm

# Windows
./gradlew buildWindowsReleaseMsi
./gradlew buildWindowsReleaseExe
./gradlew buildWindowsReleasePortable

# Web
./gradlew buildJsReleaseZip
./gradlew buildWasmJsReleaseZip
```

> **Note**: Desktop build tasks can only run on the corresponding OS and do not support cross-architecture builds. For example, `buildMacOSReleaseDmg` can only run on macOS.

Build artifacts are output to `builder/build/artifacts/`.

### Development Commands

```bash
# Run desktop app
./gradlew :app:desktop:run

# Run tests
./gradlew :app:shared:jvmTest

# Hot reload (requires JetBrains Runtime)
./gradlew :app:desktop:runHot
```

### Portable Build

To build a portable version (sets `BuildConfig.PORTABLE = true`):

```bash
./gradlew buildWindowsReleasePortable
# or explicitly:
./gradlew -Pportable=true :app:desktop:createReleaseDistributable
```

The portable flag is auto-detected when task name contains "Portable".

## Key Configuration Files

| File | Description |
|------|-------------|
| `gradle/libs.versions.toml` | Main dependency versions |
| `gradle/app.versions.toml` | App version (name, SDK versions) |
| `utils/build.gradle.kts` | BuildConfig generation |
| `builder/build.gradle.kts` | Unified build tasks for all platforms |
| `app/desktop/build.gradle.kts` | Desktop native distribution config |

## Module Dependencies

```
app:android ─┐
app:ios ─────┼─> app:shared ─> data ─> utils
app:desktop ─┤                  │
app:web ─────┘                  └─> app:app-interface
                                         │
                               app:app-datastore
```

## Architecture Notes

### Source Sets

The project uses Kotlin Multiplatform source sets:
- `commonMain` - Shared code for all platforms
- `androidMain` - Android-specific implementations
- `iosMain` - iOS-specific implementations (with `iosX64`, `iosArm64`, `iosSimulatorArm64`)
- `jvmMain` - Desktop-specific implementations
- `webMain` - Shared web code (JS + WasmJS)
- `jsMain` - JavaScript-specific code
- `wasmJsMain` - WebAssembly-specific code

### Custom Gradle Tasks (buildSrc)

| Task/Class | Description |
|------------|-------------|
| `BuildArchiveTask` | Creates ZIP archives using 7-Zip or zip fallback |
| `BuildAppImageTask` | Creates Linux AppImage packages (auto-downloads appimagetool) |
| `Target.kt` | Platform target definitions with artifact naming |
| `BuildType.kt` | Debug/Release build type definitions |
| `Format.kt` | Output format definitions (apk, ipa, dmg, msi, deb, rpm, etc.) |
| `OS.kt` | Operating system definitions |
| `Arch.kt` | Architecture definitions (x86_64, arm64/aarch64) |

### BuildConfig

Generated in `utils` module with fields:
- `APP_NAME` - "TimeFlow"
- `APP_VERSION_NAME` - from `gradle/app.versions.toml`
- `APP_VERSION_CODE` - calculated from GitHub commit count
- `BUILD_TIME` - build timestamp
- `GIT_COMMIT_HASH` - short commit hash
- `PORTABLE` - `true` for portable builds

### Desktop Configuration

- **JVM**: Uses ZGC garbage collector with 512MB soft heap limit
- **ProGuard**: Release builds use ProGuard 7.7.0 with `proguard-rules.pro`
- **Native packages**: DMG (macOS), MSI/EXE (Windows), DEB/RPM (Linux)
- **Icons**: Platform-specific icons in `app/desktop/desktopAppIcons/`

### iOS Configuration

- Framework name: `App` (static framework)
- Uses Xcode project in `iosApp/`
- Build tasks create unsigned IPA files

## Code Style

- Follow Kotlin coding conventions
- Use `expect/actual` for platform-specific implementations
- Place shared UI in `app/shared/src/commonMain/`
- License header required in all Kotlin files (AGPLv3)

## Common Tasks

### Adding a new dependency
1. Add version to `gradle/libs.versions.toml`
2. Add library/plugin definition
3. Use in module's `build.gradle.kts` with `libs.xxx`

### Creating platform-specific implementation
1. Define `expect` declaration in `commonMain`
2. Create `actual` implementations in platform source sets
3. Use kotlin-inject for DI if needed

### Exporting library definitions (for About screen)
```bash
# Android/JVM/JS/WasmJS: automatic via build tasks
# iOS: manual export required
./gradlew :app:shared:exportLibraryDefinitions \
  -PaboutLibraries.outputFile=src/iosMain/composeResources/files/libraries.json \
  -PaboutLibraries.exportVariant=metadataIosMain
```
