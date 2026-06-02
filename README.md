# PoPP-Module
Implementation of the gematik PoPP-Module specification (https://gemspec.gematik.de/prereleases/Draft_PoPP_26_1/)

This is a Kotlin Multiplatform project targeting Android and iOS. The PoPP business
logic lives in a reusable SDK library that is exported as native artifacts (an
Android `.aar` and an iOS `.xcframework`) and consumed by a Compose Multiplatform
demo host app.

## Modules

* [`/popp-sdk`](./popp-sdk/src) — Kotlin Multiplatform **SDK library** with the PoPP
  business logic and **no UI dependencies**, so it can be embedded into any host app.
  Exposes a public API (`PoppSdk`) and builds to an Android AAR and an iOS XCFramework
  (`PoppSdk`). Platform-specific code uses `expect`/`actual`: shared declarations live in
  [commonMain](./popp-sdk/src/commonMain/kotlin), and platform implementations live in
  [androidMain](./popp-sdk/src/androidMain/kotlin) / [iosMain](./popp-sdk/src/iosMain/kotlin).

* [`/popp-demo-app`](./popp-demo-app/src) — Compose Multiplatform **library** holding the
  demo host's shared UI ([commonMain/App.kt](./popp-demo-app/src/commonMain/kotlin)) and the
  iOS entry point ([iosMain/MainViewController.kt](./popp-demo-app/src/iosMain/kotlin)). It
  depends on `:popp-sdk` and produces the iOS framework `PoppDemoApp`.

* [`/androidApp`](./androidApp/src) — Thin Android **application** that hosts the shared UI
  (`MainActivity` calls `App()` from `:popp-demo-app`). This is the runnable Android app.

* [`/iosApp`](./iosApp/iosApp) — The iOS **application** (Xcode project). It is the iOS entry
  point that embeds the `PoppDemoApp` framework, and where any SwiftUI code would go.

> **Note:** Since AGP 9, a single Gradle module cannot be both a Kotlin Multiplatform module
> and an Android application (`com.android.application` is incompatible with the KMP plugin).
> That is why the runnable Android app (`:androidApp`) is a separate module from the
> multiplatform UI library (`:popp-demo-app`). On iOS the equivalent app shell is the
> `:iosApp` Xcode project.

## Running the apps

- Android app: `./gradlew :androidApp:installDebug` (build + install on a connected device/emulator),
  then launch it from the device, or:
  `adb shell monkey -p de.servicehealth.poppmodule -c android.intent.category.LAUNCHER 1`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it. (Running on a physical
  device requires setting your signing `TEAM_ID` in
  [iosApp/Configuration/Config.xcconfig](./iosApp/Configuration/Config.xcconfig); a simulator
  needs no signing.)

## Running tests

- Android (host JVM, fast): `./gradlew :popp-sdk:testAndroidHostTest`
- iOS (simulator, requires macOS + simulator): `./gradlew :popp-sdk:iosSimulatorArm64Test`

## Building the SDK artifacts

- Android AAR: `./gradlew :popp-sdk:assemble` → `popp-sdk/build/outputs/aar/`
- iOS XCFramework: `./gradlew :popp-sdk:assemblePoppSdkXCFramework` → `popp-sdk/build/XCFrameworks/`
