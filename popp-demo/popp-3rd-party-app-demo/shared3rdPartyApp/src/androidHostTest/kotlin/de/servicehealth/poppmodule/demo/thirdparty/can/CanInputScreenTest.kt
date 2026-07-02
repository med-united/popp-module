package de.servicehealth.poppmodule.demo.thirdparty.can

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import de.servicehealth.poppmodule.theme.BrandTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingCanStore(private val initial: String? = null) : CanStore {
    var saved: String? = null

    override suspend fun load(): String? = initial

    override suspend fun save(can: String) {
        saved = can
    }

    override suspend fun clear() {
        saved = null
    }
}

// Past the screen's 450ms auto-advance delay, with margin.
private const val PAST_AUTO_ADVANCE_MS = 800L

// The CAN screen is a tall, vertically scrollable column. A tall test display keeps the keypad
// and continue button within the viewport for the functional tests below, so their injected
// clicks/visibility checks land without scrolling. `keypadIsReachableByScrolling` overrides this
// with a realistic phone height to exercise the scroll itself.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h2000dp")
class CanInputScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingSixDigitsPersistsAndCompletes() =
        runComposeUiTest {
            val store = RecordingCanStore()
            var completed = false
            // Drive the clock manually so the coroutine `delay` in the auto-advance effect
            // is advanced deterministically rather than relying on auto-advance.
            mainClock.autoAdvance = false
            setContent {
                BrandTheme {
                    CompositionLocalProvider(LocalCanStore provides store) {
                        CanInputScreen(onBack = {}, onClose = {}, onComplete = { completed = true })
                    }
                }
            }
            waitForIdle()
            listOf("1", "2", "3", "4", "5", "6").forEach { onNodeWithTag("can_key_$it").performClick() }
            waitForIdle()
            mainClock.advanceTimeBy(PAST_AUTO_ADVANCE_MS)
            waitForIdle()
            assertEquals("123456", store.saved)
            assertTrue(completed)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun backspaceRemovesDigitBeforeCompletion() =
        runComposeUiTest {
            val store = RecordingCanStore()
            var completed = false
            mainClock.autoAdvance = false
            setContent {
                BrandTheme {
                    CompositionLocalProvider(LocalCanStore provides store) {
                        CanInputScreen(onBack = {}, onClose = {}, onComplete = { completed = true })
                    }
                }
            }
            waitForIdle()
            onNodeWithTag("can_key_9").performClick()
            onNodeWithTag("can_key_back").performClick()
            listOf("1", "2", "3", "4", "5", "6").forEach { onNodeWithTag("can_key_$it").performClick() }
            waitForIdle()
            mainClock.advanceTimeBy(PAST_AUTO_ADVANCE_MS)
            waitForIdle()
            assertEquals("123456", store.saved)
            assertTrue(completed)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun prefilledCanShowsContinueAndDoesNotAutoAdvance() =
        runComposeUiTest {
            val store = RecordingCanStore(initial = "123456")
            var completed = false
            setContent {
                BrandTheme {
                    CompositionLocalProvider(LocalCanStore provides store) {
                        CanInputScreen(onBack = {}, onClose = {}, onComplete = { completed = true })
                    }
                }
            }
            // A remembered CAN waits for explicit confirmation: it must not auto-advance.
            onNodeWithTag("can_continue").assertIsDisplayed()
            assertEquals(false, completed)
            onNodeWithTag("can_continue").performClick()
            waitForIdle()
            assertTrue(completed)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun changingPrefilledCanThenRetypingAdvancesWithNewCan() =
        runComposeUiTest {
            val store = RecordingCanStore(initial = "111111")
            var completed = false
            // Keep auto-advance on so the prefill `LaunchedEffect(Unit)` applies, then switch
            // to manual clock control for the timing-sensitive auto-advance delay below.
            setContent {
                BrandTheme {
                    CompositionLocalProvider(LocalCanStore provides store) {
                        CanInputScreen(onBack = {}, onClose = {}, onComplete = { completed = true })
                    }
                }
            }
            // Switch the remembered CAN to fresh manual entry, then type a different CAN.
            onNodeWithTag("can_change").performClick()
            waitForIdle()
            mainClock.autoAdvance = false
            listOf("6", "5", "4", "3", "2", "1").forEach { onNodeWithTag("can_key_$it").performClick() }
            waitForIdle()
            mainClock.advanceTimeBy(PAST_AUTO_ADVANCE_MS)
            waitForIdle()
            assertEquals("654321", store.saved)
            assertTrue(completed)
        }

    // Regression: on a real phone the keypad's bottom row sits below the fold, so the column must
    // scroll. A realistic height forces the backspace key off-screen; it must be reachable by scroll.
    @OptIn(ExperimentalTestApi::class)
    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h780dp")
    fun keypadIsReachableByScrolling() =
        runComposeUiTest {
            val store = RecordingCanStore()
            setContent {
                BrandTheme {
                    CompositionLocalProvider(LocalCanStore provides store) {
                        CanInputScreen(onBack = {}, onClose = {}, onComplete = {})
                    }
                }
            }
            waitForIdle()
            onNodeWithTag("can_key_back").performScrollTo().assertIsDisplayed()
        }
}
