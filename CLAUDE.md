# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project purpose

Implementation of the gematik PoPP-Module specification (https://gemspec.gematik.de/prereleases/Draft_PoPP_26_1/) as a Kotlin Multiplatform project with Android and iOS targets. The business logic lives in a reusable `:popp-sdk` library (exported as an Android AAR and an iOS XCFramework) so it can be embedded into arbitrary host apps; `:popp-demo-app` is the Compose Multiplatform demo host that consumes the SDK.

## Build & test commands

The Gradle daemon auto-provisions JDK 21 (Amazon Corretto) via `gradle/gradle-daemon-jvm.properties`, but Kotlin/JVM compilation targets JVM 11. Configuration cache and build cache are enabled in `gradle.properties`.

- Build Android debug APK: `./gradlew :androidApp:assembleDebug`
- Install + launch on a connected device/emulator: `./gradlew :androidApp:installDebug` then `adb shell monkey -p de.servicehealth.poppmodule -c android.intent.category.LAUNCHER 1`
- Build everything: `./gradlew build`
- Run SDK tests (Android host JVM, fast): `./gradlew :popp-sdk:testAndroidHostTest`
- Run SDK tests (iOS simulator, requires macOS + simulator): `./gradlew :popp-sdk:iosSimulatorArm64Test`
- Run a single test class: `./gradlew :popp-sdk:testAndroidHostTest --tests "de.servicehealth.poppmodule.sdk.PoppSdkTest"`
- Build the SDK Android AAR: `./gradlew :popp-sdk:assemble` (output under `popp-sdk/build/outputs/aar/`)
- Build the SDK iOS XCFramework: `./gradlew :popp-sdk:assemblePoppSdkXCFramework` (output under `popp-sdk/build/XCFrameworks/`)
- iOS app build/run: open `iosApp/iosApp.xcodeproj` in Xcode (the Kotlin framework `PoppDemoApp` is produced by the `:popp-demo-app` build).

The iOS test task name is `iosSimulatorArm64Test` (not the older `iosTest`) because only `iosArm64()` and `iosSimulatorArm64()` are declared — there is intentionally no `iosX64()` target.

## Architecture

Three Gradle modules plus an Xcode project:

- **`:popp-sdk`** — Kotlin Multiplatform library holding the PoPP business logic, with **no Compose/UI dependencies** so any host app (Compose, SwiftUI/UIKit, View-based, backend) can consume it. Uses the new AGP 9 KMP plugin (`com.android.kotlin.multiplatform.library`) with an `androidLibrary { }` block. Exposes a public API (`PoppSdk`) and produces an Android AAR plus a static iOS XCFramework named `PoppSdk`. Namespace `de.servicehealth.poppmodule.sdk`.
- **`:popp-demo-app`** — Compose Multiplatform **library** containing the demo host's shared UI (`App.kt` in `commonMain`) and the iOS entry point (`MainViewController.kt` in `iosMain`). Uses the new AGP 9 KMP plugin + Compose, depends on `:popp-sdk` via `projects.poppSdk`, and produces a static iOS framework named `PoppDemoApp`. Namespace `de.servicehealth.poppmodule.demo`.
- **`:androidApp`** — Thin Android application module (`com.android.application`). `MainActivity` calls `setContent { App() }` from `:popp-demo-app`; depends on it via `projects.poppDemoApp`. Namespace/applicationId `de.servicehealth.poppmodule`.
- **`iosApp/`** — Xcode project consuming the `PoppDemoApp` framework. `ContentView.swift` wraps `MainViewControllerKt.MainViewController()` (declared in `popp-demo-app/src/iosMain/.../MainViewController.kt`) in a `UIViewControllerRepresentable`. The Xcode run-script build phase calls `./gradlew :popp-demo-app:embedAndSignAppleFrameworkForXcode`.

### Why the app and the shared UI are separate modules (AGP 9 constraint)

Since AGP 9.0, `com.android.application` **cannot** be applied together with `org.jetbrains.kotlin.multiplatform` in the same module, and the only KMP-compatible Android plugin (`com.android.kotlin.multiplatform.library`) produces a library, not an APK. A single module therefore cannot be both a Kotlin Multiplatform iOS-framework producer and a runnable Android application. So multiplatform code (including Compose UI) lives in library modules (`:popp-sdk`, `:popp-demo-app`), and the runnable Android app is a thin plain-Android `:androidApp` that depends on them. The iOS app consumes `:popp-demo-app` directly.

### Source set layout (KMP modules)

KMP source-set folders exist only once they hold files, though Gradle recognises the standard ones by convention regardless. Current on-disk state under `<module>/src/`:

- `:popp-sdk` — `commonMain`, `androidMain`, `iosMain`, `commonTest`.
- `:popp-demo-app` — `commonMain`, `iosMain` only (its Android entry point lives in `:androidApp`, and it has no tests yet).

Roles of the standard source sets:

- `commonMain` — shared Kotlin (and Compose UI in `:popp-demo-app`'s `App.kt`); `expect` declarations.
- `androidMain` / `iosMain` — `actual` implementations and platform entry points (e.g. `Platform.android.kt`, `Platform.ios.kt` in `:popp-sdk`; `MainViewController.kt` in `:popp-demo-app`).
- `commonTest` — multiplatform tests, compiled and run on every target (e.g. `PoppSdkTest`).
- `androidHostTest` — Android-target host JVM tests (AGP 9 KMP plugin name; **not** `androidUnitTest`). **No folder yet**, but pre-configured in `:popp-sdk` via `withHostTest { isIncludeAndroidResources = true }`; create `src/androidHostTest/` when you need tests that use Android-specific APIs.
- `iosTest` — iOS-simulator tests (task `iosSimulatorArm64Test`). **No folder yet**; create `src/iosTest/` when needed.

### Platform abstraction pattern

Cross-platform code uses Kotlin's `expect`/`actual`. In `:popp-sdk`, common code declares `expect fun getPlatform(): Platform`; each platform source set provides an `actual` implementation (`Platform.android.kt`, `Platform.ios.kt`). Follow this pattern when adding platform-specific behavior needed for the PoPP-Module spec.

## Conventions

- Package root: `de.servicehealth.poppmodule` (SDK code under `de.servicehealth.poppmodule.sdk`).
- Android namespaces: `:popp-sdk` → `de.servicehealth.poppmodule.sdk`, `:popp-demo-app` → `de.servicehealth.poppmodule.demo`, `:androidApp` → `de.servicehealth.poppmodule`.
- Dependencies are managed through the Gradle version catalog at `gradle/libs.versions.toml` — add libraries/plugins there rather than inlining versions in build scripts.
- Type-safe project accessors are enabled (`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` in `settings.gradle.kts`), so reference modules as `projects.poppSdk` / `projects.poppDemoApp`.
- Kotlin code style: official (`kotlin.code.style=official` in `gradle.properties`).
