# Third-party notices

## gematik E-Rezept-App-Android

The files under
`popp-sdk/src/androidMain/kotlin/de/servicehealth/poppmodule/sdk/egk/nfc/internal/`
that carry a gematik copyright header, and the correspondingly marked tests under
`popp-sdk/src/androidHostTest/kotlin/de/servicehealth/poppmodule/sdk/egk/nfc/internal/`,
are derived from the gematik E-Rezept-App-Android project
(https://github.com/gematik/E-Rezept-App-Android),
Copyright gematik GmbH, licensed under the EUPL, Version 1.2.

Modifications by the PoPP-Module project (POPPM-119): repackaged to
`de.servicehealth.poppmodule.sdk.egk.nfc.internal`, trimmed to the
PACE / secure-messaging subset needed for the PoPP eGK proxy role (the upstream
HealthCardCommand DSL, status-word tables, file-system/identifier objects and the
EF.Version2 card-version gate are not ported — plain handshake commands are built by
this project's own `PaceCommands`), visibility reduced to `internal`, `@Requirement`
annotations removed, and wrong-CAN detection added to the PACE exchange.

The derived files are distributed under this repository's GPLv2 license, as
permitted by EUPL-1.2 Article 5 and its Appendix, which lists
"GNU General Public License (GPL) v. 2, v. 3" as a Compatible Licence.
