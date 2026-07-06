# Manual testing: App-to-App flow

The App-to-App flow lets a third-party app hand off to the insurance app via OS-level
deep-link navigation, using PKCE/PAR for the OIDC leg. Neither demo domain
(`idp.insurance.popp.demo` for the hand-off, `demo.popp.de` for the callback) is a real
server, so Android App Links verification fails for both. On Android 12+, this causes the
OS to route both deep links to a browser instead of the target app. A one-time `adb`
workaround per install — on both apps — is required. This page walks through the full
flow, the workaround, and what each step should produce.

## Current implementation status

| Leg | Status | Notes |
| --- | --- | --- |
| PAR request (3rd-party → IDP) | **Mocked** | Any URL containing `idp.demo.gematik.de` returns `urn:uuid:mock-request-uri` after an 800 ms simulated delay |
| OS hand-off (3rd-party → insurance) | **Working** | Requires App Links workaround below |
| Inbound deep-link handling (insurance) | **Working** | `AppToAppSession.handleDeepLink` parses and validates the URL |
| User consent UI (insurance) | **Working** | Alert dialog — "Simulate Success" or "Cancel / Reject" |
| Callback redirect (insurance → 3rd-party) | **Working** | Returns `mock_auth_code_12345`; error path returns `access_denied` |
| Token exchange (3rd-party) | **Mocked** | Same `idp.demo.gematik.de` guard; returns `token_exchange_ok` |

## Prerequisites

- Physical Android phone, USB-debugging enabled (`adb devices` shows it).
- Both demo apps installed:
  - `de.servicehealth.poppmodule.demo.thirdparty` — 3rd-party app
  - `de.servicehealth.poppmodule.demo.insurance` — insurance app
- Both apps must be the current build (state does not survive a reinstall — see step 1).

## 1. Fix Android App Links verification (required after every reinstall)

Neither demo domain has a real server to serve `assetlinks.json`. On Android 12+, an
unverified App Links domain means the OS stops routing HTTPS URLs to the app entirely and
falls back to a browser. Two domains need to be approved — one per app:

| Domain | App | Symptom if missing |
| --- | --- | --- |
| `idp.insurance.popp.demo` | insurance app | "App nicht installiert" alert when switching apps |
| `demo.popp.de` | 3rd-party app | Callback opens in browser instead of resuming the app |

Run all four commands after installing (or reinstalling) either app:

```bash
# Approve the hand-off domain for the insurance app
adb shell pm set-app-links --package de.servicehealth.poppmodule.demo.insurance 1 idp.insurance.popp.demo

# Approve the callback domain for the 3rd-party app
adb shell pm set-app-links --package de.servicehealth.poppmodule.demo.thirdparty 1 demo.popp.de
```

Confirm both are verified:

```bash
adb shell pm get-app-links de.servicehealth.poppmodule.demo.insurance
# → idp.insurance.popp.demo: verified

adb shell pm get-app-links de.servicehealth.poppmodule.demo.thirdparty
# → demo.popp.de: verified
```

**Why `1` and not `3`?** State `1` = `STATE_SUCCESS` (verified). State `3` = `STATE_DENIED`
— the OS will show that domain as `denied` and still route to a browser.

**This approval is lost on every reinstall.** Re-run all four commands whenever either
app is reinstalled.

## 2. Walk through the flow

### Step A — Start PAR in the 3rd-party app

Open the 3rd-party app and navigate to the App-zu-App screen. Tap **"App-zu-App-Flow
starten"**. After ~800 ms the button is replaced by a success line showing
`request_uri: urn:uuid:mock-request-uri` and a second button appears.

### Step B — Hand off to the insurance app

Tap **"Zur Krankenkassen-App wechseln"**. The OS resolves
`https://idp.insurance.popp.demo/app-to-app/auth?…` and opens the insurance app
directly (no disambiguation dialog, no browser). The insurance app comes to the
foreground.

If the "App nicht installiert" alert appears instead, the App Links workaround from
step 1 was not applied or the insurance app was reinstalled since then.

### Step C — Consent in the insurance app

The insurance app shows an alert: **"App2App Check-in Request"** identifying
`demo-3rd-party-app` as the requesting client.

- **"Simuliere Erfolg"** → redirects back to the 3rd-party app with
  `code=mock_auth_code_12345&state=<state>`.
- **"Ablehnen"** → redirects back with `error=access_denied&state=<state>`.

Dismissing the dialog (back gesture) also takes the reject path.

### Step D — Callback lands in the 3rd-party app

The redirect URI `https://demo.popp.de/callback?…` is handled by the 3rd-party app's
intent filter. The 3rd-party app resumes and the callback is processed. Token exchange
is mocked (the `idp.demo.gematik.de` guard in `OidcParClient` short-circuits to
`token_exchange_ok`).

## 3. Build and install commands

```bash
# Build and install both apps
./gradlew :popp-demo:popp-3rd-party-app-demo:android3rdPartyApp:installDebug
./gradlew :popp-demo:popp-insurance-app-demo:androidInsuranceApp:installDebug

# Apply App Links workaround (re-run after every reinstall of either app)
adb shell pm set-app-links --package de.servicehealth.poppmodule.demo.insurance 1 idp.insurance.popp.demo
adb shell pm set-app-links --package de.servicehealth.poppmodule.demo.thirdparty 1 demo.popp.de
```

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| "App nicht installiert" alert in 3rd-party app | App Links not approved — run the `pm set-app-links` command from step 1 |
| `idp.insurance.popp.demo: denied` in `pm get-app-links` | Wrong state was set (3 = denied, not approved) — run `pm set-app-links … 1 …` to correct it |
| Insurance app does not come to the foreground | The OS sent the intent to a browser — the App Links state reverted after a reinstall |
| Alert dialog does not appear in insurance app | Deep link parsed but `client_id` validation failed, or the intent was not delivered via `onNewIntent` (check that `launchMode="singleTask"` is in the insurance app manifest) |
| Consent buttons open the browser with `https://demo.popp.de/callback?…` | `demo.popp.de` App Links not approved for the 3rd-party app — run `adb shell pm set-app-links --package de.servicehealth.poppmodule.demo.thirdparty 1 demo.popp.de` |
| PAR step shows a network error | The `DEMO_PAR_ENDPOINT` constant does not contain `idp.demo.gematik.de` — the mock guard was bypassed and a real network call was made to a non-existent server |

## Known limitations

- **PAR and token exchange are mocked end-to-end.** The `request_uri` and auth code are
  hardcoded; no real IDP is involved.
- **`client_id` validation in `AppToAppSession.isValidClient` accepts any non-empty
  string.** The `demo-3rd-party-app` check is illustrative only.
- **The App Links workaround is per-device and per-install.** A production deployment
  would serve real `assetlinks.json` files at both `idp.insurance.popp.demo` and
  `demo.popp.de` so Android verifies automatically.
- **The fingerprints in `web/*/assetlinks.json` are debug-only.** The current fingerprint
  (`34:7B:11:1B:…`) comes from the local debug keystore. Apps distributed via Google Play
  are re-signed by Google (Play App Signing), so the fingerprint changes. The production
  fingerprint is available in Play Console → your app → **Setup → App integrity → App
  signing key certificate → SHA-256 certificate fingerprint**. Both fingerprints can
  coexist in the `sha256_cert_fingerprints` array to support debug and release builds
  simultaneously.
