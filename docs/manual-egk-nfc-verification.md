# Manual verification: NFC eGK channel (POPPM-119)

The IsoDep transport and real-card PACE cannot run in CI. Verify on hardware once the
POPPM-135 UI exists (CAN input + scan screen); until then, an ad-hoc reader-mode activity
in a demo app can drive the same call.

1. Start the local PoPP stack (see the "Run eGK WebSocket IT" notes / WebSocketScenarioTransportIT):
   docker stack with the PoPP-Server, WebSocket endpoint `ws://localhost:8443`
   (the ingress `/ws` route is ZETA-gated and returns 401 — connect directly).
2. Install a demo app on a physical NFC-capable Android phone (`adb reverse tcp:8443 tcp:8443`
   so the device reaches the local server).
3. In reader mode, on tag discovery:
   `sdk.checkInWithEgk(EgkNfcChannel.fromTag(tag, can)) { p -> log(p) }`
4. Expected: EgkProgress advances over the scenario steps; result is
   `EgkCheckInResult.Success(token, pn)` from the local server.
5. Negative checks: wrong CAN → `PoppSdkError.Card(WRONG_CAN)`; pulling the card away
   mid-read → `PoppSdkError.Card(CARD_LOST)`.
