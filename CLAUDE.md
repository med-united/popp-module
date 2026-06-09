# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project purpose

Implementation of the gematik PoPP-Module specification (https://gemspec.gematik.de/prereleases/Draft_PoPP_26_1/) as a Kotlin Multiplatform project with Android and iOS targets. The business logic lives in a reusable `:popp-sdk` library (exported as an Android AAR and an iOS XCFramework) so it can be embedded into arbitrary host apps. The `popp-demo/` folder holds the demo: a common UI library (`popp-demo:shared`) plus **two** host-app demos (a 3rd-party app and an insurance app) that each consume the SDK.

## Build & test commands

The Gradle daemon auto-provisions JDK 21 (Amazon Corretto) via `gradle/gradle-daemon-jvm.properties`, but Kotlin/JVM compilation targets JVM 11. Configuration cache and build cache are enabled in `gradle.properties`.

- Build everything: `./gradlew build`
- 3rd-party demo Android app: `./gradlew :popp-demo:popp-3rd-party-app-demo:android3rdPartyApp:installRiseDebug` then `adb shell monkey -p de.servicehealth.poppmodule.demo.thirdparty -c android.intent.category.LAUNCHER 1` (flavors: `local`, `rise` [default], `ru`, `pu` — substitute in task name as `install{Flavor}Debug`)
- Insurance demo Android app: `./gradlew :popp-demo:popp-insurance-app-demo:androidInsuranceApp:installDebug` then `adb shell monkey -p de.servicehealth.poppmodule.demo.insurance -c android.intent.category.LAUNCHER 1`
- Run SDK tests (Android host JVM, fast): `./gradlew :popp-sdk:testAndroidHostTest`
- Run SDK tests (iOS simulator, requires macOS + simulator): `./gradlew :popp-sdk:iosSimulatorArm64Test`
- Run a single test class: `./gradlew :popp-sdk:testAndroidHostTest --tests "de.servicehealth.poppmodule.sdk.PoppSdkTest"`
- Code coverage (aggregated over `:popp-sdk` + `:popp-demo:shared`): `./gradlew :popp-sdk:testAndroidHostTest :popp-demo:shared:testAndroidHostTest :koverXmlReport :koverHtmlReport` (output under `build/reports/kover/`)
- Build the SDK Android AAR: `./gradlew :popp-sdk:assemble` (output under `popp-sdk/build/outputs/aar/`)
- Build the SDK iOS XCFramework: `./gradlew :popp-sdk:assemblePoppSdkXCFramework` (output under `popp-sdk/build/XCFrameworks/`)
- iOS apps: open the relevant Xcode project, e.g. `popp-demo/popp-3rd-party-app-demo/ios3rdPartyApp/iosApp.xcodeproj`

The iOS test task name is `iosSimulatorArm64Test` (not the older `iosTest`) because only `iosArm64()` and `iosSimulatorArm64()` are declared — there is intentionally no `iosX64()` target.

## Architecture

One SDK module plus a `popp-demo/` group (common lib + two host-app demos):

- **`:popp-sdk`** — Kotlin Multiplatform library holding the PoPP business logic, with **no Compose/UI dependencies** so any host app (Compose, SwiftUI/UIKit, View-based, backend) can consume it. New AGP 9 KMP plugin (`com.android.kotlin.multiplatform.library`). Public API `PoppSdk`; produces an Android AAR + static iOS XCFramework `PoppSdk`. Namespace `de.servicehealth.poppmodule.sdk`.
- **`:popp-demo:shared`** — Compose Multiplatform **library**: common demo UI (service·health brand theme in `…/theme`, `BrandShowcaseScreen` in `…/demo`, which renders the SDK-integration proof) + TWK Everett fonts via Compose resources (`packageOfResClass = de.servicehealth.poppmodule.demo.generated.resources`). Depends on `:popp-sdk`. No iOS framework of its own (consumed by the per-app modules). Namespace `de.servicehealth.poppmodule.demo`.
- **3rd-party demo** under `popp-demo/popp-3rd-party-app-demo/`:
  - `:popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp` -> Compose-MP library; `App.kt` (commonMain) + `MainViewController.kt` (iosMain) in package `…demo.thirdparty`; depends on `projects.poppDemo.shared`; static iOS framework `Shared3rdPartyApp`. Namespace `…demo.thirdparty.shared`.
  - `:popp-demo:popp-3rd-party-app-demo:android3rdPartyApp` -> `com.android.application`; `MainActivity` (package `…demo.thirdparty`) calls `App()`. Namespace/applicationId `de.servicehealth.poppmodule.demo.thirdparty`.
  - `ios3rdPartyApp/` -> Xcode project; `ContentView.swift` `import Shared3rdPartyApp`; run-script `cd "$SRCROOT/../../.."` then `./gradlew :popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:embedAndSignAppleFrameworkForXcode`. Bundle id `…demo.thirdparty`.
- **Insurance demo** under `popp-demo/popp-insurance-app-demo/` -> identical shape: `sharedInsuranceApp` (framework `SharedInsuranceApp`, package/namespace `…demo.insurance[.shared]`), `androidInsuranceApp` (id `…demo.insurance`), `iosInsuranceApp`.

Dependency flow per app: `androidXApp → sharedXApp → popp-demo:shared → :popp-sdk`

### Why each app and its shared UI are separate modules (AGP 9 constraint)

Since AGP 9.0, `com.android.application` **cannot** be applied together with `org.jetbrains.kotlin.multiplatform` in one module, and the KMP-compatible Android plugin (`com.android.kotlin.multiplatform.library`) produces a library, not an APK. So a module can't be both a KMP iOS-framework producer and a runnable Android app. Hence each demo splits into a KMP library (`sharedXApp`, also the iOS framework producer), a thin `com.android.application` (`androidXApp`), and an Xcode project (`iosXApp`).

### Source set layout (KMP modules)

KMP source-set folders exist only once they hold files, though Gradle recognises the standard ones by convention regardless. Current state:

- `:popp-sdk` — `commonMain`, `androidMain`, `iosMain`, `commonTest`.
- `:popp-demo:shared` — `commonMain` + `commonTest`.
- `sharedXApp` modules — `commonMain/.../App.kt` + `iosMain/.../MainViewController.kt` only.

Roles: `commonMain` (shared Kotlin + Compose UI; `expect` decls), `androidMain`/`iosMain` (`actual` impls + platform entry points), `commonTest` (multiplatform tests, run on every target). `androidHostTest` (Android-target host JVM tests; AGP 9 name, **not** `androidUnitTest`) and `iosTest` exist by convention but have **no folder yet**; `androidHostTest` is pre-configured in `:popp-sdk` and `:popp-demo:shared` via `withHostTest { isIncludeAndroidResources = true }`.

### Platform abstraction pattern

Cross-platform code uses Kotlin's `expect`/`actual`. In `:popp-sdk`, common code declares `expect fun getPlatform(): Platform`; each platform source set provides an `actual` (`Platform.android.kt`, `Platform.ios.kt`). Follow this pattern when adding platform-specific behavior needed for the PoPP-Module spec.

## Code coverage & CI

Kover is applied at the root and aggregates coverage over `:popp-sdk` and `:popp-demo:shared` (the modules with host tests). Compose-generated classes are filtered out of the reports via root Kover excludes (`*.generated.resources.*`, `*ComposableSingletons*`). `.github/workflows/code-coverage.yml` runs both modules' `testAndroidHostTest` plus the root `:koverXmlReport`/`:koverHtmlReport`, uploads artifacts, and pushes the XML report to Codecov. `.github/dependabot.yml` watches Gradle dependencies weekly.

## Conventions

- Package root: `de.servicehealth.poppmodule` (SDK under `.sdk`; demo common under `.demo`; per-app under `.demo.thirdparty` / `.demo.insurance`).
- Dependencies are managed through the Gradle version catalog at `gradle/libs.versions.toml` — add libraries/plugins there rather than inlining versions in build scripts.
- Type-safe project accessors are enabled (`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` in `settings.gradle.kts`), so reference modules as `projects.poppSdk`, `projects.poppDemo.shared`, `projects.poppDemo.popp3rdPartyAppDemo.shared3rdPartyApp`
- Kotlin code style: official (`kotlin.code.style=official` in `gradle.properties`).
