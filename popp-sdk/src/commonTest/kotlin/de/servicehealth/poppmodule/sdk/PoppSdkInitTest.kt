@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package de.servicehealth.poppmodule.sdk

import de.servicehealth.poppmodule.sdk.internal.ZetaEngine
import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

/**
 * Pins the POPPM-115 / A_28507 behavior: init(fqdn) itself performs the ZETA
 * client initialisation (engine start), not the first functional call.
 */
class PoppSdkInitTest {

    private val fqdn = "wss://popp.example.test"

    private fun TestScope.sdkWith(factory: RecordingEngineFactory): PoppSdk = PoppSdk(
        context = null,
        storageOverride = InMemoryStorage(),
        engineFactory = factory::create,
        sdkScope = this,
    )

    @Test
    fun init_alone_starts_the_zeta_engine() = runTest {
        val factory = RecordingEngineFactory()
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        advanceUntilIdle()

        assertEquals(1, factory.engines.size, "init() must create the device engine")
        assertEquals(1, factory.engines[0].startCalls, "init() must start the engine")
        assertEquals(fqdn, factory.configs[0].fqdn)
        assertEquals(DeviceOnly, factory.configs[0].tokenProvider, "init() must warm up the device engine")
    }

    @Test
    fun second_init_with_same_fqdn_is_a_noop() = runTest {
        val factory = RecordingEngineFactory()
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        advanceUntilIdle()
        sdk.init(fqdn)
        advanceUntilIdle()

        assertEquals(1, factory.engines.size)
        assertEquals(1, factory.engines[0].startCalls)
    }

    @Test
    fun second_init_with_different_fqdn_throws_configuration() = runTest {
        val factory = RecordingEngineFactory()
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        val error = assertFailsWith<PoppSdkError.Configuration> {
            sdk.init("wss://other.example.test")
        }
        assertTrue(error.message!!.contains(fqdn))
        advanceUntilIdle()
        assertEquals(1, factory.engines.size)
    }

    @Test
    fun init_then_status_creates_exactly_one_engine() = runTest {
        val factory = RecordingEngineFactory()
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        // The pending warm-up must reuse the engine that status() created and cached.
        assertEquals("STARTED", sdk.status())
        advanceUntilIdle()

        assertEquals(1, factory.engines.size)
        assertEquals(1, factory.engines[0].startCalls)
    }

    @Test
    fun warmup_failure_surfaces_on_first_functional_call() = runTest {
        val factory = RecordingEngineFactory(failAllStarts = true)
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        advanceUntilIdle()
        assertEquals(1, factory.engines.size, "warm-up must have been attempted")

        assertFailsWith<PoppSdkError.Network> { sdk.status() }
        assertEquals(2, factory.engines.size, "status() must retry with a fresh engine")
    }

    @Test
    fun failed_warmup_is_retried_and_recovers() = runTest {
        val factory = RecordingEngineFactory(failFirstStart = true)
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        advanceUntilIdle()
        assertEquals(1, factory.engines.size)

        assertEquals("STARTED", sdk.status())
        assertEquals(2, factory.engines.size)

        assertEquals("STARTED", sdk.status())
        assertEquals(2, factory.engines.size, "successful engine must be cached")
    }

    @Test
    fun status_during_inflight_warmup_waits_and_reuses_engine() = runTest {
        val gate = CompletableDeferred<Unit>()
        val factory = RecordingEngineFactory(startGate = gate)
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        testScheduler.runCurrent() // warm-up now holds deviceMutex, suspended in start()
        val status = async { sdk.status() }
        testScheduler.runCurrent() // status() is now waiting on the mutex
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals("STARTED", status.await())
        assertEquals(1, factory.engines.size, "in-flight warm-up must be reused, not duplicated")
        assertEquals(1, factory.engines[0].startCalls)
    }

    @Test
    fun second_init_after_failed_warmup_does_not_relaunch() = runTest {
        val factory = RecordingEngineFactory(failFirstStart = true)
        val sdk = sdkWith(factory)

        sdk.init(fqdn)
        advanceUntilIdle()
        sdk.init(fqdn)
        advanceUntilIdle()

        assertEquals(1, factory.engines.size, "same-FQDN re-init must be a no-op, not a relaunch")
    }
}

internal class RecordingZetaEngine(
    private val failStart: Boolean,
    private val startGate: CompletableDeferred<Unit>? = null,
) : ZetaEngine {
    var startCalls = 0
        private set

    override suspend fun start() {
        startCalls++
        startGate?.await()
        if (failStart) throw PoppSdkError.Network("simulated start failure")
    }

    override suspend fun status(): String = "STARTED"

    override suspend fun hello(): String = "hello"
}

internal class RecordingEngineFactory(
    private val failFirstStart: Boolean = false,
    private val failAllStarts: Boolean = false,
    private val startGate: CompletableDeferred<Unit>? = null,
) {
    val engines = mutableListOf<RecordingZetaEngine>()
    val configs = mutableListOf<PoppSdkConfig>()

    fun create(config: PoppSdkConfig, storage: SecureStorage): ZetaEngine {
        configs += config
        val fail = failAllStarts || (failFirstStart && engines.isEmpty())
        return RecordingZetaEngine(fail, startGate).also { engines += it }
    }
}

internal class InMemoryStorage : SecureStorage {
    private val map = mutableMapOf<String, String>()
    override suspend fun put(key: String, value: String) { map[key] = value }
    override suspend fun get(key: String): String? = map[key]
    override suspend fun remove(key: String) { map.remove(key) }
    override suspend fun clear() { map.clear() }
}
