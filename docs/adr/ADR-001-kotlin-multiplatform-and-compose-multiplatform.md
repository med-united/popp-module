# ADR-001: Selection of Kotlin Multiplatform (KMP) and Compose Multiplatform

* **Status:** **Accepted**
* **Date:** 2026-05-20
* **Driver:** Norman Tietz / Lead Developer

## Context and Problem Statement

The Proof of Patient Presence (PoPP) module to be developed serves as a client-side frontend component that must be
flexibly used in mobile applications (health insurance apps and third-party apps such as e-prescription or video
consultation solutions) with various technology stacks on iOS and Android platforms.

Extremely strict functional, regulatory, and security framework conditions apply due to gematik.
A double, completely separate implementation of the heavy cryptographic and network logic for iOS and Android
drastically increases the risk of errors with specification changes. Pure hybrid frameworks (such as classic web views)
are excluded due to the tough hardware requirements (NFC, native camera interfaces) and the ZETA attestation
requirements.

## Decision Outcome

We opt to use **Kotlin Multiplatform (KMP)** for the architecture of the SDK core, combined with
**Compose Multiplatform** for the cross-platform user interface.

1. **Isolated SDK Module:** The PoPP module is implemented as an independent, reusable KMP library module (
   `:popp-sdk`), compiled as `.aar` for Android and `.xcframework` for iOS.
2. **Shared Logic in `commonMain`:** All state-controlling logic is written once in pure Kotlin.
3. **Hardware Integration via `expect/actual`:** For hardware-near eGK-NFC communication and linking the
   camera video stream, the common code declares platform-independent interfaces (`expect`), whose concrete
   implementations (`actual`) are natively implemented.
4. **Unified UI via Compose:** The standardized dialogs are declaratively implemented using Compose Multiplatform
   to ensure identical user guidance and error-free terminology on both platforms.

## Consequences

* **Positive:**
    * Single Source of Truth: Changes to the critical gematik specifications (e.g., adjustment of timeout periods,
      crypto algorithms, or JSON payload structures) only need to be maintained in one place in the shared code.
    * No Compromises on Security: By accessing platform-native network engines, strict TLS and certificate checks can be
      implemented uncompromisingly.
    * Native Hardware Performance: Since KMP compiles to native machine code for iOS, the time-critical APDU command
      loops over NFC run without performance losses caused by JavaScript or Cordova bridges.
    * High UI Consistency: The strict text and button specifications of gematik can be localized centrally and rendered
      identically across platforms.
* **Negative/Risks:**
    * Increased interoperability effort in the UI layer: For the camera viewfinder of the QR scanner, a native bridge
      must be built on iOS because Compose Multiplatform does not abstract the iOS camera out-of-the-box.
    * Learning curve for iOS developers: iOS developers need to be familiar with both Kotlin and Swift, whereas Kotlin
      is more familiar or standard for Android developers.

## Pros and Cons of the Options

### Option 1: Kotlin Multiplatform (KMP) + Compose Multiplatform *(chosen)*

* **Pros:**
    * Single Source of Truth for all business logic, cryptographic routines, and gematik spec compliance — one
      change propagates to both platforms.
    * Compiles to native machine code on both Android and iOS; no JavaScript or Cordova bridge overhead in the
      time-critical NFC APDU loops.
    * Full access to platform-native TLS stacks and certificate pinning via `expect/actual`, satisfying strict
      security requirements without compromise.
    * Compose Multiplatform delivers identical UI rendering on both platforms, making gematik-mandated text and
      button specifications easy to enforce centrally.
    * Ships as standard `.aar` (Android) and `.xcframework` (iOS) artifacts — straightforward integration for
      third-party host apps regardless of their technology stack.
* **Cons:**
    * Compose Multiplatform for iOS is still maturing; certain components (e.g., camera viewfinder) require custom
      native bridges.
    * iOS developers must learn Kotlin; the team needs competence in both ecosystems.
    * Slightly higher build complexity compared to single-platform projects (Gradle multi-module, CocoaPods/SPM
      integration).

### Option 2: Separate Native Implementations (Android + iOS)

* **Pros:**
    * Full, unrestricted access to every platform API without any abstraction layer.
    * No cross-platform tooling risk; each team works in their primary language (Kotlin/Swift).
* **Cons:**
    * All business logic, cryptographic algorithms, and gematik spec compliance code must be maintained twice —
      each specification change doubles the implementation and review effort.
    * High risk of behavioral divergence between platforms (e.g., subtle differences in timeout handling or JSON
      serialisation), which is unacceptable under gematik's strict regulatory requirements.
    * Rejected due to the unacceptably high error risk and maintenance cost for safety-critical logic.

### Option 3: Flutter (Dart)

* **Pros:**
    * Single codebase for UI and application logic with good compilation performance via Dart's ahead-of-time
      compiler.
    * Large open-source ecosystem and active community.
* **Cons:**
    * Dart is not Kotlin: no code sharing with existing Android library code; platform channels are required for
      every native API (NFC, camera, secure storage).
    * Integration as `.aar`/`.xcframework` into arbitrary host apps is non-trivial — Flutter expects to own the
      top-level embedding, which conflicts with the SDK distribution model.
    * Lower adoption in the German healthcare ecosystem makes long-term support harder.
    * Rejected because it does not reduce duplication relative to native implementations and complicates SDK
      distribution.

### Option 4: React Native / Capacitor (JavaScript-based hybrid)

* **Pros:**
    * Web developers can contribute; large npm ecosystem.
    * Potential for reuse of web-based UI components.
* **Cons:**
    * The JavaScript bridge introduces latency that is incompatible with time-sensitive NFC APDU communication.
    * Native crypto engine access and ZETA device attestation require deep native modules, negating most of the
      cross-platform benefit.
    * WebView-based rendering is explicitly excluded by gematik hardware and attestation requirements.
    * Rejected as categorically unsuitable before detailed evaluation.