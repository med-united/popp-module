package de.servicehealth.poppmodule.demo.thirdparty.nfc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NfcProgressEstimatorTest {
    @Test
    fun first_step_is_positive_and_below_100() {
        val p = NfcProgressEstimator().onStep()
        assertTrue(p in 1..99, "expected 1..99 but was $p")
    }

    @Test
    fun is_monotonic_non_decreasing() {
        val e = NfcProgressEstimator()
        var prev = 0
        repeat(20) {
            val now = e.onStep()
            assertTrue(now >= prev, "step $it went backwards: $prev -> $now")
            prev = now
        }
    }

    @Test
    fun never_exceeds_cap_before_success() {
        val e = NfcProgressEstimator(stepCap = 95, perStep = 12)
        repeat(50) { e.onStep() }
        assertEquals(95, e.onStep())
    }
}
