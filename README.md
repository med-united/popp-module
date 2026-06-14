# PoPP-Module
Implementation of the gematik PoPP-Module specification (https://gemspec.gematik.de/prereleases/Draft_PoPP_26_1/)

This is a Kotlin Multiplatform project targeting Android and iOS. The PoPP business
logic lives in a reusable SDK library that is exported as native artifacts (an
Android `.aar` and an iOS `.xcframework`) and consumed by **two** Compose
Multiplatform demo host apps, to show the SDK embedded in different host contexts.

## Modules

* [`/popp-sdk`](./popp-sdk/src) -> Kotlin Multiplatform **SDK library** with the PoPP
  business logic and **no UI dependencies**, so it can be embedded into any host app.
  Exposes a public API (`PoppSdk`) and builds to an Android AAR and an iOS XCFramework
  (`PoppSdk`). Platform-specific code uses `expect`/`actual`: shared declarations live in
  [commonMain](./popp-sdk/src/commonMain/kotlin), and platform implementations live in
  [androidMain](./popp-sdk/src/androidMain/kotlin) / [iosMain](./popp-sdk/src/iosMain/kotlin).

* `/popp-demo` -> Folder grouping the demo:
  * [`/popp-demo/shared`](./popp-demo/shared/src) -> Compose Multiplatform **library** with the
    common demo UI (service·health brand theme + `BrandShowcaseScreen`, which renders the
    PoPP-SDK integration proof). Depends on `:popp-sdk`. Reused by both demo apps.
  * `/popp-demo/popp-3rd-party-app-demo` -> the **3rd-party host app** demo:
    * `shared3rdPartyApp` -> Compose-MP library (App UI + iOS entry point; depends on `popp-demo:shared`; iOS framework `Shared3rdPartyApp`)
    * `android3rdPartyApp` -> runnable Android application (id `…demo.thirdparty`)
    * `ios3rdPartyApp` -> iOS application (Xcode project)
  * `/popp-demo/popp-insurance-app-demo` -> the **insurance host app** demo, same shape:
    `sharedInsuranceApp` (framework `SharedInsuranceApp`), `androidInsuranceApp` (id `…demo.insurance`), `iosInsuranceApp`.

Dependency flow per app: `androidXApp → sharedXApp → popp-demo:shared → :popp-sdk`;
on iOS the `iosXApp` Xcode project embeds the `sharedXApp` framework.

> **Note:** Since AGP 9, a single Gradle module cannot be both a Kotlin Multiplatform module
> and an Android application (`com.android.application` is incompatible with the KMP plugin).
> So each demo app splits into a KMP library (`sharedXApp`, also the iOS framework producer)
> and a thin `com.android.application` (`androidXApp`); the iOS app shell is the Xcode project.

## Running the apps

3rd-party demo:
- Android: `./gradlew :popp-demo:popp-3rd-party-app-demo:android3rdPartyApp:installDebug`,
  then `adb shell monkey -p de.servicehealth.poppmodule.demo.thirdparty -c android.intent.category.LAUNCHER 1`
- iOS: open `popp-demo/popp-3rd-party-app-demo/ios3rdPartyApp/iosApp.xcodeproj` in Xcode and run.

Insurance demo:
- Android: `./gradlew :popp-demo:popp-insurance-app-demo:androidInsuranceApp:installDebug`,
  then `adb shell monkey -p de.servicehealth.poppmodule.demo.insurance -c android.intent.category.LAUNCHER 1`
- iOS: open `popp-demo/popp-insurance-app-demo/iosInsuranceApp/iosApp.xcodeproj` in Xcode and run.

Running on a physical iOS device requires setting your signing `TEAM_ID` in the app's
`Configuration/Config.xcconfig`; a simulator needs no signing.

## Running tests

- SDK, Android host JVM (fast): `./gradlew :popp-sdk:testAndroidHostTest`
- SDK, iOS simulator (requires macOS + simulator): `./gradlew :popp-sdk:iosSimulatorArm64Test`
- Demo shared UI, Android host JVM: `./gradlew :popp-demo:shared:testAndroidHostTest` (`BrandColorsTest`)
- 3rd-party app, Android host JVM: `./gradlew :popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest`
- All host tests at once: `./gradlew :popp-sdk:testAndroidHostTest :popp-demo:shared:testAndroidHostTest :popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest`

## Code coverage (Kover) & CI

- Local aggregated report: `./gradlew :popp-sdk:testAndroidHostTest :popp-demo:shared:testAndroidHostTest :koverXmlReport :koverHtmlReport`
  → XML `build/reports/kover/report.xml`, HTML `build/reports/kover/html/`.
- Coverage aggregates `:popp-sdk`, `:popp-demo:shared`, and `:popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp`. Compose-generated
  classes and `AndroidQrScanner` are excluded via root Kover filters.
- CI: `.github/workflows/code-coverage.yml` runs both modules' host tests, uploads artifacts, and pushes the
  XML to Codecov. Dependabot (`.github/dependabot.yml`) watches Gradle dependencies weekly.

## Building the SDK artifacts

- Android AAR: `./gradlew :popp-sdk:assemble` → `popp-sdk/build/outputs/aar/`
- iOS XCFramework: `./gradlew :popp-sdk:assemblePoppSdkXCFramework` → `popp-sdk/build/XCFrameworks/`

## Architecture decisions (ADRs)

Key decisions about *why* the project is structured this way — and what alternatives were considered and rejected —
are documented as Architecture Decision Records in [`docs/adr/`](./docs/adr/). To learn more, read the specific [`ADR Documentation`](./docs/adr/README.md) 

ADRs with status **Accepted** are binding — code or architecture that contradicts them requires a superseding ADR first.

## Code style (ktlint)

Kotlin formatting is enforced by [ktlint](https://pinterest.github.io/ktlint/) via the `jlleitschuh/ktlint-gradle` plugin.

- Check: `./gradlew ktlintCheck`
- Auto-fix: `./gradlew ktlintFormat`
- CI: the `ktlintCheck` step in `.github/workflows/code-coverage.yml` runs before tests and fails the build on any violation.

**Pre-commit hook** — run once after cloning to block commits with style violations:

```sh
printf '#!/bin/sh\n./gradlew ktlintCheck --daemon --quiet\n' > .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit
```

