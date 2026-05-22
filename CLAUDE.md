# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project purpose

Implementation of the gematik PoPP-Module specification (https://gemspec.gematik.de/prereleases/Draft_PoPP_26_1/) as a Kotlin Multiplatform library with Android and iOS targets. At the time of writing the repo is a fresh KMP scaffold (default "Click me!" Compose template) — `Greeting` / `Platform` / `App` are placeholder code that should be replaced as PoPP functionality is added.

## Build & test commands

The Gradle daemon auto-provisions JDK 21 (Amazon Corretto) via `gradle/gradle-daemon-jvm.properties`, but Kotlin/JVM compilation targets JVM 11. Configuration cache and build cache are enabled in `gradle.properties`.

- Build Android debug APK: `./gradlew :androidApp:assembleDebug`
- Build everything: `./gradlew build`
- Run shared module tests (Android host JVM, fast): `./gradlew :shared:testAndroidHostTest`
- Run shared module tests (iOS simulator, requires macOS + simulator): `./gradlew :shared:iosSimulatorArm64Test`
- Run a single test class: `./gradlew :shared:testAndroidHostTest --tests "de.servicehealth.poppmodule.SharedLogicAndroidHostTest"`
- iOS app build/run: open `iosApp/iosApp.xcodeproj` in Xcode (the Kotlin framework `Shared` is produced by the `:shared` build).

The iOS test task name is `iosSimulatorArm64Test` (not the older `iosTest`) because only `iosArm64()` and `iosSimulatorArm64()` are declared — there is intentionally no `iosX64()` target.

## Architecture

Two Gradle modules plus an Xcode project:

- **`:shared`** — Kotlin Multiplatform library containing all cross-platform code, including Compose Multiplatform UI in `commonMain`. Uses the new AGP 9 KMP plugin (`com.android.kotlin.multiplatform.library`) with an `androidLibrary { }` block instead of the legacy `android { }` block. Produces a static iOS framework named `Shared`.
- **`:androidApp`** — Thin Android application module. `MainActivity` calls `setContent { App() }` from `:shared`. Depends on `:shared` via the type-safe accessor `projects.shared` (enabled by `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` in `settings.gradle.kts`).
- **`iosApp/`** — Xcode project consuming the `Shared` framework. `ContentView.swift` wraps `MainViewControllerKt.MainViewController()` (declared in `shared/src/iosMain/.../MainViewController.kt`) in a `UIViewControllerRepresentable`.

### Source set layout (`shared/src/`)

- `commonMain` — shared Kotlin + Compose UI (`App.kt`), platform abstractions (`expect fun getPlatform()`).
- `androidMain` / `iosMain` — `actual` implementations and platform entry points.
- `commonTest` — multiplatform tests.
- `androidHostTest` — Android-flavored host JVM tests (AGP 9 KMP plugin name; **not** `androidUnitTest`). Android resources are included via `withHostTest { isIncludeAndroidResources = true }`.
- `iosTest` — iOS simulator tests.

### Platform abstraction pattern

Cross-platform code uses Kotlin's `expect`/`actual`. Common code declares `expect fun getPlatform(): Platform`; each platform source set provides an `actual` implementation (`Platform.android.kt`, `Platform.ios.kt`). Follow this pattern when adding platform-specific behavior (crypto, secure storage, NFC, etc.) needed for the PoPP-Module spec.

## Conventions

- Package root: `de.servicehealth.poppmodule` (Android namespace `de.servicehealth.poppmodule.shared` for the library).
- Dependencies are managed through the Gradle version catalog at `gradle/libs.versions.toml` — add libraries/plugins there rather than inlining versions in build scripts.
- Kotlin code style: official (`kotlin.code.style=official` in `gradle.properties`).