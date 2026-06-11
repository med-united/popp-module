# Manual verification: NFC eGK channel (POPPM-119)

The `IsoDepTransport` and real-card PACE cannot run in CI — they need a physical NFC
phone and a real eGK. This walkthrough drives the full chain
(`EgkNfcChannel.fromTag` → PACE → secure messaging → PoPP scenario loop → token)
against the local dockerized PoPP stack, using an ad-hoc reader-mode activity in the
3rd-party demo app. Once the POPPM-135 UI (CAN input + scan screen) exists, it replaces
the ad-hoc activity and steps 3–4 below.

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
# socket on its own, so point DOCKER_HOST at it:
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

## 3. Temporary test wiring in the SDK (revert afterwards!)

`PoppSdk.checkInWithEgk` requires a *started* SDK (`PoppSdk.start(...)` runs the ZETA
registration/attestation), but the local stack is reached directly, bypassing ZETA —
and no local ZETA Guard config exists yet. Until the ZETA-authenticated transport
lands, patch `popp-sdk/src/commonMain/kotlin/de/servicehealth/poppmodule/sdk/PoppSdk.kt`
in two marked places:

1. In the no-arg constructor, set the service URL (the device reaches the host's
   stack via the `adb reverse` below, so `localhost` is correct *on the phone*):

   ```kotlin
   constructor() : this(
       engine = null,
       poppServiceUrl = "ws://localhost:8443/ws",   // TEMP(manual-egk-test), was: null
       ...
   ```

2. In `checkInWithEgk`, comment out the started-SDK gate:

   ```kotlin
   // TEMP(manual-egk-test): local stack is reached directly, no ZETA engine needed
   // if (engine == null) {
   //     throw PoppSdkError.Configuration("PoppSdk not started — call PoppSdk.start() first")
   // }
   ```

Both lines are load-bearing in production — grep for `TEMP(manual-egk-test)` and revert
before committing anything.

## 4. Ad-hoc reader-mode activity in the 3rd-party demo

Add the NFC permission to
`popp-demo/popp-3rd-party-app-demo/android3rdPartyApp/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.NFC" />
```

Replace the body of
`popp-demo/popp-3rd-party-app-demo/android3rdPartyApp/src/main/kotlin/de/servicehealth/poppmodule/MainActivity.kt`
with a reader-mode variant (keep the original `setContent { App() }` line):

```kotlin
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.nfc.EgkNfcChannel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val TAG = "EgkNfcTest"
private const val CAN = "123456" // ← the 6-digit CAN printed on YOUR card

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private val scope = MainScope()
    private val sdk = PoppSdk() // works only with the TEMP patches from step 3

    // ... existing onCreate with setContent { App() } ...

    override fun onResume() {
        super.onResume()
        // Reader mode: eGKs are ISO-DEP over NFC-A/B; skip NDEF so discovery is instant.
        NfcAdapter.getDefaultAdapter(this)?.enableReaderMode(
            this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null,
        )
    }

    override fun onPause() {
        NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
        super.onPause()
    }

    override fun onTagDiscovered(tag: Tag) {
        Log.i(TAG, "tag discovered: ${tag.techList.joinToString()}")
        val channel = try {
            EgkNfcChannel.fromTag(tag, CAN)
        } catch (e: PoppSdkError.Card) {
            Log.e(TAG, "not an eGK? ${e.message}"); return
        }
        scope.launch {
            try {
                val result = sdk.checkInWithEgk(channel) { p ->
                    Log.i(TAG, "progress: scenario ${p.scenario}, step ${p.index + 1}/${p.count}")
                }
                Log.i(TAG, "RESULT: $result")
            } catch (e: PoppSdkError.Card) {
                Log.e(TAG, "card error: ${e.reason} — ${e.message}")
            } catch (e: PoppSdkError) {
                Log.e(TAG, "sdk error: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
```

## 5. Install and run

```bash
cd ~/git/popp-module
./gradlew :popp-demo:popp-3rd-party-app-demo:android3rdPartyApp:installDebug
# Make localhost:8443 ON THE PHONE reach the host's stack (re-run after replugging USB):
adb reverse tcp:8443 tcp:8443
adb shell monkey -p de.servicehealth.poppmodule.demo.thirdparty -c android.intent.category.LAUNCHER 1
adb logcat -s EgkNfcTest
```

Hold the eGK flat against the back of the phone (NFC antenna is usually centred) and
keep it still — PACE plus the scenario takes a few seconds.

## 6. Expected output

```
tag discovered: android.nfc.tech.IsoDep, android.nfc.tech.NfcA
progress: scenario 1, step 1/4
progress: scenario 1, step 2/4
...
RESULT: Success(poppToken=eyJ..., pruefnachweis=...)
```

The first `progress` line only appears after PACE succeeded (the handshake runs lazily
inside the first `transceive`). `RESULT: Failed(code=..., detail=...)` is also a pass
for POPPM-119 if progress advanced first — it means the server rejected the card
business-wise, not that the channel failed.

## 7. Negative checks

- **Wrong CAN:** set `CAN` to a wrong value, rebuild, scan →
  `card error: WRONG_CAN — PACE mutual authentication failed …`. Note ~1 in 256
  handshakes legitimately failed this way before the FE2OS shared-secret fix
  (`paceSharedSecret`); with the fix a correct CAN must never produce WRONG_CAN.
- **Card lost:** pull the card away while `progress` lines are appearing →
  `card error: CARD_LOST — lost connection to the eGK during the NFC exchange`.

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `PoppSdkError.Configuration: PoppSdk not started` | Step-3 patches missing (or reverted too early). |
| `sdk error: …Connection refused` | `adb reverse` not active (re-run it) or the docker stack is down. |
| HTTP 401 on connect | You pointed at the ingress (`wss://…443/ws`); use `ws://localhost:8443/ws` directly. |
| `not an eGK? NFC tag does not support ISO-DEP…` | Wrong card type tapped (the message is accurate). |
| `WRONG_CAN` with the correct CAN | Double-check the printed CAN (not the Kartennummer / ICCSN). |
| `SECURE_CHANNEL_FAILED: eGK secure channel failure: null` | Card answered the EF.CardAccess read without a body — likely not a (working) eGK. |
| `CARD_LOST` immediately on every attempt | Antenna alignment; remove the phone case; keep the card still. |
| Server replies `Error`/`Failed` instantly | Check `docker compose logs popp-server`; the stack may lack trust material for your card generation. |

## Cleanup

```bash
git checkout -- popp-sdk/src/commonMain/kotlin/de/servicehealth/poppmodule/sdk/PoppSdk.kt
git checkout -- popp-demo/popp-3rd-party-app-demo/android3rdPartyApp/
cd ~/git/popp-sample-code && docker compose -f docker/compose.yaml down
```
