# Manual verification: NFC eGK channel (POPPM-119)

The `IsoDepTransport` and real-card PACE cannot run in CI — they need a physical NFC
phone and a real eGK. This walkthrough drives the full chain
(`EgkNfcChannel.fromTag` → PACE → secure messaging → PoPP scenario loop → token)
against the local dockerized PoPP stack, through the 3rd-party demo's real check-in flow
(POPPM-157 CAN screen → POPPM-161 NFC scan screen). The earlier ad-hoc reader-mode
activity and the TEMP SDK patches are gone — see §3–§4.

**What counts as "verified":** the PACE handshake completes against a real card and the
scenario steps execute over secure messaging (the progress log advances). Whether the
run ends in `Success(token, pn)` or a server-side `Failed(...)` depends on the local
stack's trust material for your specific card — both prove the channel works.

## Prerequisites

- Physical Android phone with NFC, USB-debugging enabled (`adb devices` shows it).
- A real eGK (G2.1, contactless) and its **CAN** — the 6-digit number printed on the
  front of the card.
- Docker (Docker Desktop on this machine — note the `DOCKER_HOST` quirk below).
- The gematik PoPP sample checkout providing the server stack: `~/git/popp-sample-code`.

## 1. Build and start the local PoPP stack

```bash
cd ~/git/popp-sample-code
# Build the local images. The fabric8 docker-maven-plugin can't find Docker Desktop's
# socket on its own, so point DOCKER_HOST at it. Skip this (slow) step if the images
# already exist from a previous run — check with `docker images | grep local/popp`:
DOCKER_HOST=unix://$HOME/.docker/desktop/docker.sock \
  ./mvnw install -Dskip.dockerbuild=false -DskipTests=true
# Start everything except popp-client (the `full` profile needs an SMC-B key we don't have):
docker compose -f docker/compose.yaml up -d
docker compose -f docker/compose.yaml ps   # all services Up?
```

The endpoint to use is the PoPP-Server **directly**: `ws://localhost:8443/ws`.
Do NOT use the documented ingress `wss://localhost:443/ws` — its `/ws` route is
ZETA-gated (`pep on`) and rejects an unauthenticated upgrade with HTTP 401; an
authenticated ZETA transport is a separate follow-up.

## 2. Smoke-test the stack from the host (no phone yet)

The existing manual IT proves connect + framing + (de)serialization against the stack:

```bash
cd ~/git/popp-module
POPP_WS_URL="ws://localhost:8443/ws" ./gradlew --no-daemon \
  :popp-sdk:testAndroidHostTest --tests "*WebSocketScenarioTransportIT"
```

(`--no-daemon` matters: a pre-existing Gradle daemon caches its own environment and the
test would silently skip.) If this is green, every later failure is on the NFC side.

## 3. SDK wiring — no patching needed (POPPM-119 + POPPM-161)

This flow is productised: there are **no TEMP patches**. The 3rd-party demo's `MainActivity`
constructs the SDK from the `local` product flavor's `BuildConfig.POPP_SERVER_FQDN` and
configures it:

```kotlin
val poppSdk = PoppSdk(PoppSdkContext(applicationContext))
poppSdk.init(BuildConfig.POPP_SERVER_FQDN)
// → App(poppSdk = poppSdk, canStore = …)
```

`checkInWithEgk` then runs the real eGK read loop straight against the configured FQDN over
the **direct** WebSocket transport — it needs only `init(fqdn)`, not a started ZETA engine,
because ZETA routing for the eGK loop is dormant (a separate follow-up, POPPM-180). Until that
lands the local stack is reached directly at `ws://localhost:8443/ws`. Nothing to edit.

> Earlier revisions used a fenced `PoppSdk.directTransport(...)` (`@PoppDevTransport`) factory.
> That was dropped when POPPM-119 landed: `checkInWithEgk` is already ZETA-free, so the bypass
> was redundant — plain `init(fqdn)` is the path now.

## 4. The real screen (replaces the old ad-hoc activity)

The hand-written reader-mode activity is gone. NFC is now owned by the demo's own code:

- `NfcReaderEgkChannelSource` (`shared3rdPartyApp` androidMain) drives `NfcAdapter` reader
  mode (`FLAG_READER_NFC_A | NFC_B | SKIP_NDEF_CHECK`) and hands each tag to
  `EgkNfcChannel.fromTag(tag, can)`.
- `NfcScanScreen` (commonMain) runs `checkInWithEgk` via `NfcCheckInController` and renders
  live progress; the CAN comes from the encrypted `CanStore` filled on the CAN screen.
- The NFC permission and the `local` flavor's `BuildConfig.POPP_SERVER_FQDN` are already in
  `android3rdPartyApp` (`uses-permission android.permission.NFC`).

## 5. Install and run

```bash
cd ~/git/popp-module
./gradlew :popp-demo:popp-3rd-party-app-demo:android3rdPartyApp:installLocalDebug
# Make localhost:8443 ON THE PHONE reach the host's stack (re-run after replugging USB):
adb reverse tcp:8443 tcp:8443
adb shell monkey -p de.servicehealth.poppmodule.demo.thirdparty -c android.intent.category.LAUNCHER 1
adb logcat   # PoppSdk* / errors; progress itself is shown on-screen
```

In the app: launcher → start demo → QR → **CAN** (enter your card's 6-digit CAN) → **NFC
scan**. Hold the eGK flat against the back of the phone (NFC antenna is usually centred)
and keep it still — PACE plus the scenario takes a few seconds. The scan screen shows the
percentage climbing, then routes to the Success or Error screen.

## 6. Expected behaviour

On a good tap the scan screen shows "Karte erkannt …", the **Sichere Übertragung · N %**
pill climbs as the SDK emits `EgkProgress` for each transceive, and then the flow routes to
the **Success** screen (real `poppToken` + `pruefnachweis`) or the **Error** screen.

PACE runs lazily *inside* the first transceive, so the percentage starts moving even when
PACE then fails (`WRONG_CAN`, `CARD_LOST`). The proof PACE **succeeded** is the progress
advancing past the first scenario's first step.

Against this local stack a real card typically lands on the **Error** screen with
`SERVER_REJECTED` (`UnknownCertificates`) — the server lacks trust material for the card
generation. That is still a pass for the channel (the card was read end-to-end); to get a
real `Success`, pre-seed the hash as in §6a.

## 6a. Getting a real Success: pre-seed the eGK hash (local stack)

The PoPP server only issues a token when the card's certificate-pair hash is known. On every
tap it computes `cvcHash = SHA-256(end-entity CVC)` and `autHash = SHA-256(X.509 AUT cert)` —
the raw card responses with the status word stripped (`ScenarioResultStep` keeps the SW in a
separate field) — and looks the pair up in the `egk_entries` table
(`AuthG2ScenarioResultProcessor.checkCertificatePair` → `EgkHashValidationService`). Found and
not `BLOCKED` → `MATCH` → token. The **contactless** path does **not** auto-enrol an unknown
card (unlike contact mode), and nothing is imported by default, so a fresh test eGK is always
`UnknownCertificates`. The two hashes are **card-specific** — each tester seeds their own card.

This procedure is verified end-to-end (RU test eGK, 2026-06-16). The DB row survives
`popp-server` rebuilds/recreations (the `egk_hash_db` volume is separate) but is lost on
`docker compose down -v`.

**1. Make popp-server log the hashes (one-time, kept as a dev aid).**
`popp-server` `EgkHashValidationService.validateAndProcess` already carries a `DEV AID` log line
that prints them (commented, points back here). If your `popp-sample-code` checkout doesn't have
it, add after the two `computeSHA256(...)` calls:
```java
import java.util.HexFormat;
// DEV AID (local stack): log the cert-pair hashes so a real test eGK can be pre-seeded.
log.info("| {} eGK cert-pair hashes (hex) — cvcHash={} autHash={}",
    sessionId, HexFormat.of().formatHex(cvcHash), HexFormat.of().formatHex(autHash));
```

**2. Rebuild + recreate just popp-server** (Docker Desktop on this host → set `DOCKER_HOST`;
the active context has no `/var/run/docker.sock`):
```bash
cd ~/git/popp-sample-code
DOCKER_HOST=unix://$HOME/.docker/desktop/docker.sock \
  ./mvnw -q install -pl popp-server -Dskip.dockerbuild=false -DskipTests=true
docker compose -f docker/compose.yaml up -d --force-recreate --no-deps popp-server
# wait until: curl -s -o /dev/null -w '%{http_code}' http://localhost:8443/  → 404
```

**3. Tap once to capture the hashes** (it still ends in `SERVER_REJECTED`), then read them:
```bash
docker logs popp-server 2>&1 | grep "cert-pair hashes" | tail -1
# → ... cvcHash=<64 hex> autHash=<64 hex>
```

**4. Seed the row** (state `imported`, never-expiring; via the db container):
```bash
docker exec popp-server-db psql -U poppserver -d egk_hash_db -c \
  "INSERT INTO egk_entries (cvc_hash, aut_hash, state, not_after) VALUES \
   (decode('<cvcHex>','hex'), decode('<autHex>','hex'), 'imported', '2099-01-01 00:00:00');"
```

**5. Tap again → Success.** The server logs `Found entry in the database: EgkEntry(... state=IMPORTED)`
then `Generated PoPP-Token for the client: eyJ...`; the app receives
`EgkCheckInResult.Success(poppToken, pruefnachweis)` and shows the **Success** screen
("Karte gelesen" / "Verifiziert").

> Tip: re-assert `adb reverse tcp:8443 tcp:8443` and check `adb shell curl localhost:8443` (→ 404)
> right before each tap — the UsbFfs reverse can silently drop and surfaces as a `NETWORK` error.

## 7. Negative checks

- **Wrong CAN:** enter a wrong CAN on the CAN screen, then scan → Error screen with
  `WRONG_CAN` (PACE mutual authentication failed). Note ~1 in 256 handshakes legitimately
  failed this way before the FE2OS shared-secret fix (`paceSharedSecret`); with the fix a
  correct CAN must never produce WRONG_CAN.
- **Card lost:** pull the card away while the percentage is climbing → Error screen with
  `CARD_LOST` (lost connection to the eGK during the NFC exchange).

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `PoppSdkError.Configuration: PoppSdk not initialised — call init(fqdn) first` | `MainActivity` didn't call `init(...)`, or wasn't run with the `local` flavor; check `poppSdk.init(BuildConfig.POPP_SERVER_FQDN)`. |
| Error screen `NETWORK` / `…Connection refused` | `adb reverse tcp:8443 tcp:8443` not active (re-run it) or the docker stack is down. |
| HTTP 401 on connect | You pointed at the ingress (`wss://…443/ws`); use `ws://localhost:8443/ws` directly. |
| `not an eGK? NFC tag does not support ISO-DEP…` | Wrong card type tapped (the message is accurate). |
| `WRONG_CAN` with the correct CAN | Double-check the printed CAN (not the Kartennummer / ICCSN). |
| `SECURE_CHANNEL_FAILED: eGK secure channel failure: null` | Card answered the EF.CardAccess read without a body — likely not a (working) eGK. |
| `CARD_LOST` immediately on every attempt | Antenna alignment; remove the phone case; keep the card still. |
| A tap produces **nothing** in the log (no `tag discovered`) | The screen dozed/locked → `onPause` disabled reader mode. Wake the phone and bring the app to the foreground before tapping (`adb shell svc power stayon usb` keeps it awake while on USB). |
| Server replies `Error`/`Failed` instantly | Check `docker compose logs popp-server`; the stack may lack trust material for your card generation. |

## Cleanup

No source to revert (the flow is productised — `init(fqdn)` + `checkInWithEgk` over the
direct transport). Just stop the stack when done:

```bash
cd ~/git/popp-sample-code && docker compose -f docker/compose.yaml down
```
