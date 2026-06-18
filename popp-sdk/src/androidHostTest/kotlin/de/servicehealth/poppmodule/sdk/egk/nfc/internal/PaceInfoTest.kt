/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 *
 * Modified by the PoPP-Module project (POPPM-119) — see NOTICE.md at the repository root.
 */

package de.servicehealth.poppmodule.sdk.egk.nfc.internal

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.PaceInfo
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert
import kotlin.test.Test

class PaceInfoTest {
    @Test
    fun testPaceInfoExtraction() {
        val cardAccessBytes: ByteArray = Hex.decode("31143012060A04007F0007020204020202010202010D")
        val expectedProtocolId = "0.4.0.127.0.7.2.2.4.2.2"
        val expectedPaceInfoProtocolBytes: ByteArray = Hex.decode("04007F00070202040202")
        val paceInfo = PaceInfo(cardAccessBytes)
        val protocolId = paceInfo.protocolID
        Assert.assertEquals(expectedProtocolId, protocolId)
        val paceInfoProtocolBytes = paceInfo.paceInfoProtocolBytes
        Assert.assertArrayEquals(
            expectedPaceInfoProtocolBytes,
            paceInfoProtocolBytes,
        )
    }

    @Test
    fun unsupportedParameterIdIsRejectedWithMeaningfulError() {
        // Same EF.CardAccess structure but with an unknown PACE parameter id (0x63) instead of 0x0D.
        // The curve lookup must fail fast with a clear error rather than a downstream NPE.
        val cardAccessBytes: ByteArray = Hex.decode("31143012060A04007F00070202040202020102020163")
        try {
            PaceInfo(cardAccessBytes)
            Assert.fail("Unknown PACE parameterID should be rejected")
        } catch (e: IllegalArgumentException) {
            Assert.assertTrue(e.message!!.contains("parameterID"))
        }
    }
}
