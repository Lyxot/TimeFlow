# CLAUDE.md

## Project Overview

TimeFlow is a schedule/timetable app built with Kotlin Multiplatform + Compose Multiplatform, targeting Android, iOS,
Desktop, and Web from a single codebase. It includes an optional API server for cloud sync.

## Project Structure

```
TimeFlow/
├── app/
│   ├── android/          # Android entry point
│   ├── ios/              # iOS entry point
│   ├── desktop/          # Desktop (JVM) entry point
│   ├── web/              # Web (JS/WasmJS) entry point
│   ├── shared/           # Shared UI and business logic
│   ├── app-datastore/    # DataStore persistence (Android/iOS/Desktop)
│   ├── app-localstorage/ # localStorage persistence (Web only, JS/WasmJS)
│   └── app-interface/    # Shared repository interfaces
├── api/
│   ├── server/           # Ktor backend (PostgreSQL, JWT, Flyway)
│   ├── models/           # Shared KMP request/response DTOs
│   └── client/           # KMP HTTP client SDK
├── data/                 # Data models and serialization
├── utils/                # Utilities and BuildConfig generation
├── builder/              # Unified build tasks for all platforms
├── buildSrc/             # Custom Gradle tasks (BuildArchiveTask, BuildAppImageTask, etc.)
└── gradle/               # Version catalogs (libs.versions.toml, app.versions.toml)
```

## Module Dependencies

```
app:android ─┐
app:ios ─────┼─> app:shared ─> data ─> utils
app:desktop ─┤       │
app:web ─────┘       └─> api:client ─> api:models ─> data
                     └─> app:app-interface
                              │
                    app:app-datastore (Android/iOS/Desktop)
                    app:app-localstorage (Web)

api:server ─> api:models ─> data
```

## Build & Dev Commands

```bash
# Run desktop app
./gradlew :app:desktop:run

# Run tests
./gradlew :app:shared:jvmTest
./gradlew :api:server:test

# Build (convention: build{Target}{BuildType}{Format})
./gradlew :builder:buildAndroidReleaseApk
./gradlew :builder:buildMacOSReleaseDmg
./gradlew :builder:buildLinuxReleaseAppImage
./gradlew :builder:buildWindowsReleaseMsi
./gradlew :builder:buildWebCompatReleaseZip
./gradlew :api:server:buildFatJar
```

Desktop builds only run on the corresponding OS. Artifacts output to `builder/build/artifacts/`.

## Code Style

- Follow Kotlin coding conventions
- Use `expect/actual` for platform-specific implementations
- Place shared UI in `app/shared/src/commonMain/`
- License header required in all Kotlin files (AGPLv3)
- Dependencies: add to `gradle/libs.versions.toml`, use as `libs.xxx` in build scripts
