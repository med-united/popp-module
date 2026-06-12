# ADR-001: Selection of Kotlin Multiplatform (KMP) and Compose Multiplatform

* **Status:** **Accepted**

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