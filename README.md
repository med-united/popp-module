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
- Android: `./gradlew :popp-demo:popp-3rd-party-app-demo:android3rdPartyApp:installRiseDebug`,
  then `adb shell monkey -p de.servicehealth.poppmodule.demo.thirdparty -c android.intent.category.LAUNCHER 1`
- iOS: open `popp-demo/popp-3rd-party-app-demo/ios3rdPartyApp/iosApp.xcodeproj` in Xcode and run.

Insurance demo:
- Android: `./gradlew :popp-demo:popp-insurance-app-demo:androidInsuranceApp:installRiseDebug`,
  then `adb shell monkey -p de.servicehealth.poppmodule.demo.insurance -c android.intent.category.LAUNCHER 1`
- iOS: open `popp-demo/popp-insurance-app-demo/iosInsuranceApp/iosApp.xcodeproj` in Xcode and run.

Running on a physical iOS device requires setting your signing `TEAM_ID` in the app's
`Configuration/Config.xcconfig`; a simulator needs no signing.

### Selecting the PoPP-Server (Android product flavors)

Both Android demo apps (`android3rdPartyApp` and `androidInsuranceApp`) define a
`popp_server` flavor dimension that controls which PoPP-Server the app connects to.
The selected flavor injects a `BuildConfig.POPP_SERVER_FQDN` string, which the app
passes to `PoppSdk.init(fqdn)` at startup.

| Flavor | Target environment | FQDN |
|--------|--------------------|------|
| `local` | Local docker-compose stack (`popp-sample-code`) with ZetaGuard + PoPP-Server | `wss://popp-zeta-ingress:443/ws` |
| `rise` | RISE intermediate PoPP-Server (dev environment) | `wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc` |
| `ru` | gematik RU PoPP-Server | TBD |
| `pu` | gematik PU PoPP-Server | TBD |

The flavor name is inserted between the module path and the build type when invoking
Gradle tasks, following the standard Android convention
`<flavorName><BuildType>` (e.g. `riseDebug`).

**Available `install*` tasks for each app** (`./gradlew tasks --group install` to list all):

| Task suffix           | Flavor + build type |
|-----------------------|---------------------|
| `installLocalDebug`   | `local` + `debug`   |
| `installRiseDebug`    | `rise` + `debug`    |
| `installRuDebug`      | `ru` + `debug`      |
| `installPuRelease`    | `pu` + `release`    |
| …                     | …                   |

> **Tip:** In Android Studio, open the *Build Variants* panel (`View → Tool Windows → Build Variants`)
> and select the desired variant (e.g. `riseDebug`) before running or debugging the app.

## Running tests

- SDK, Android host JVM (fast): `./gradlew :popp-sdk:testAndroidHostTest`
- SDK, iOS simulator (requires macOS + simulator): `./gradlew :popp-sdk:iosSimulatorArm64Test`
- Demo shared UI, Android host JVM: `./gradlew :popp-demo:shared:testAndroidHostTest` (`BrandColorsTest`)
- 3rd-party app, Android host JVM: `./gradlew :popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest`
- All host tests at once: `./gradlew :popp-sdk:testAndroidHostTest :popp-demo:shared:testAndroidHostTest :popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest`

### On-device tests

Verify implementations on a connected device or emulator. This is the complement to the host JVM tests, which use a
software-backed stub to keep CI fast.

**Prerequisites:** a device or emulator connected via adb (`adb devices` to confirm).

```bash
./gradlew :popp-sdk:connectedAndroidDeviceTest
```

> **Note:** These tests are not part of CI (CI has no connected device). Run them manually
> before changing code that relies on the device.

### Integration testing

Integration tests live in `popp-sdk/src/androidHostTest` alongside the unit tests but are
excluded from the default `testAndroidHostTest` task. They exercise the full SDK stack —
ZETA registration, authentication, and the `hello()` call — against a real PoPP-Server over
the network.

#### Running integration tests

Pass `-Pintegration` (Gradle project property) together with `-Dpopp.integration.fqdn=<wss://...>`:

* RISE dev server (publicly trusted certificate — no extra flags needed):
  ```bash
  ./gradlew :popp-sdk:testAndroidHostTest \
    -Pintegration \
    -Dpopp.integration.fqdn="wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc"
  ```

* Local docker-compose stack (self-signed certificate — supply the CA cert):
  ```bash
  ./gradlew :popp-sdk:testAndroidHostTest \
    -Pintegration \
    -Dpopp.integration.fqdn="wss://popp-zeta-ingress:443/ws" \
    -Dpopp.integration.ca.pem.file="/absolute/path/to/ca.pem"
  ```

The FQDN values match the Android product flavors defined in the demo apps (see [Selecting the PoPP-Server](#selecting-the-popp-server-android-product-flavors)).

##### Obtaining the CA certificate for the local stack

The docker-compose stack uses a self-signed CA. Export it once and pass the **absolute** path via `-Dpopp.integration.ca.pem.file`. Note the local stack cert currently has no Subject Alternative Names, which causes OkHttp hostname verification to fail — the cert needs to be regenerated with `DNS:popp-zeta-ingress` as a SAN before local stack tests will pass.

```bash
openssl s_client -connect popp-zeta-ingress:443 -showcerts \
  </dev/null 2>/dev/null | openssl x509 -outform PEM > /absolute/path/to/ca.pem
```

#### How separation works

- Without `-Pintegration`, `testAndroidHostTest` applies `exclude("**/*IntegrationTest*")`,
  so only unit tests run. CI is unaffected.
- With `-Pintegration`, the task switches to `include("**/*IntegrationTest*")`, running only
  integration tests.
- If `-Dpopp.integration.fqdn` is omitted the test fails immediately with a clear message,
  so accidentally running with `-Pintegration` alone is safe.

### Visual snapshot testing (Roborazzi)

Composable previews are rendered to PNG images using [Roborazzi](https://github.com/takahirom/roborazzi) on top of Robolectric — no emulator required.

- **`:popp-demo:shared`** — every `@Preview`-annotated function is captured automatically via `SharedPreviewScreenshotTest` (component-level, natural preview size).
- **`:popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp`** — full screens are captured via `ScreenSnapshotTest` at 1080×2424 px (360 dp × xxhdpi).

**Typical local workflow — capture before/after a UI change:**

```bash
# 1. Record baseline before making changes
./gradlew recordSnapshots

# 2. Make UI changes …

# 3. Re-run in compare mode — diff PNGs written to build/outputs/roborazzi/ on any mismatch
./gradlew :popp-demo:shared:testAndroidHostTest
./gradlew :popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest

# 4. (Optional) Hard-fail the build if anything changed visually
./gradlew verifySnapshots
```

PNG output lives under each module's `build/outputs/roborazzi/` (git-ignored).
CI generates fresh snapshots on every push/PR via `.github/workflows/visual-snapshots.yml` and uploads them as the `snapshot-images` artifact (30-day retention) for visual review.

### Code coverage (Kover) & CI

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
./gradlew addKtlintCheckGitPreCommitHook
```

