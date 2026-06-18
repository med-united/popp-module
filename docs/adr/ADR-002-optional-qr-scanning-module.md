# ADR-002: Optional QR scanning module (`:popp-sdk-qr`)

* **Status:** **Proposed**
* **Date:** 2026-06-18
* **Driver:** Beatriz Correia

## Context and Problem Statement

`:popp-sdk` declared CameraX (`androidx.camera.*`) and ML Kit (`com.google.mlkit:barcode-scanning`) in its `androidMain` dependencies, so every consumer inherited camera/ML hardware dependencies, the camera permission merge, and the associated binary size. This violates the SDK's stated contract of having no UI/hardware dependencies.

## Decision Outcome

We will extract `AndroidQrScanner` and all CameraX/ML Kit dependencies into a separate, opt-in Gradle module `:popp-sdk-qr`. `:popp-sdk` retains only the pure-Kotlin QR abstraction: `PoppQrScanner`, `ScanResult`, `PoppCheckInPayload`, and `CheckInQrParser`, so the common abstraction is always available with zero hardware dependencies.

Consumers that need scanning must opt in explicitly with `implementation(projects.poppSdkQr)`; consumers that do not, get no camera/ML Kit in their dependency tree.

`:popp-sdk` must never re-declare CameraX or ML Kit. This is enforced by an automated guard test (`NoCameraDependencyTest`) that fails if those classes reappear on the SDK's classpath.

## Consequences

* **Positive:**
    * Apps that do not scan QR codes, ship no CameraX/ML Kit, no merged camera permission, and a smaller APK.
    * The SDK's no hardware dependencies contract is restored.
    * Clean compile-time separation with no runtime guards or reflection needed.
    * The regression test (`NoCameraDependencyTest`) makes the boundary self-enforcing in CI.
* **Negative/Risks:**
    * One additional Gradle module to maintain (`settings.gradle.kts` include + `build.gradle.kts`).
    * The package `de.servicehealth.poppmodule.sdk.qr` is now split across two modules (abstraction in `:popp-sdk`, Android implementation in `:popp-sdk-qr`).
    * Consumers must remember the extra dependency declaration to enable scanning.
    * `IosQrScanner` (AVFoundation, no external dependency) currently remains in `:popp-sdk`. The split is therefore Android-only for now. Moving the iOS implementation into `:popp-sdk-qr` for symmetry is a possible follow-up but would add XCFramework wiring for iOS consumers.

## Pros and Cons of the Options

### Option 1: Separate module `:popp-sdk-qr` *(chosen)*

* **Pros:**
    * Clean compile-time separation with no runtime guards or reflection.
    * Apps that do not add the `:popp-sdk-qr` module are unaffected: no extra APK size, no camera entries merged into their manifest, and no third-party build/optimization (R8/ProGuard) rules.
    * Follows a proven, common pattern. Popular libraries (Coil, Retrofit, Room) already ship optional features as separate add-on modules included only when needed.
    * The module boundary is enforceable in CI via a dependency guard test.
* **Cons:**
    * Slightly more upfront setup: a new `settings.gradle.kts` include and a new `build.gradle.kts`.
    * The QR package is split across two modules.

### Option 2: `compileOnly` scoping with documented runtime dependencies

* **Pros:**
    * Keeps the SDK a single, compact module.
* **Cons:**
    * `AndroidQrScanner` compiles against APIs that are not bundled, requiring a `try/catch(NoClassDefFoundError)` guard at instantiation and explicit documentation telling each host which transitive dependencies to add.
    * Fragile: scanning fails at runtime if a consumer forgets a dependency.
    * Rejected as too error-prone for an SDK boundary.
