package de.servicehealth.poppmodule.demo.thirdparty.can

import de.servicehealth.poppmodule.sdk.storage.SecureStorage

/** SecureStorage namespace for the 3rd-party app (distinct from the SDK's own "popp-sdk"). */
const val CAN_NAMESPACE = "popp-demo-3rd-party"

private const val CAN_KEY = "egk_can"

/** Persists a single remembered CAN for pre-fill on the next check-in. */
interface CanStore {
    suspend fun load(): String?

    suspend fun save(can: String)

    suspend fun clear()
}

/** Encrypted-at-rest [CanStore] backed by the SDK [SecureStorage]. */
class SecureStorageCanStore(private val secureStorage: SecureStorage) : CanStore {
    override suspend fun load(): String? = secureStorage.get(CAN_KEY)

    override suspend fun save(can: String) = secureStorage.put(CAN_KEY, can)

    override suspend fun clear() = secureStorage.remove(CAN_KEY)
}

/** Process-lifetime [CanStore] for Compose previews and tests. */
class InMemoryCanStore : CanStore {
    private var value: String? = null

    override suspend fun load(): String? = value

    override suspend fun save(can: String) {
        value = can
    }

    override suspend fun clear() {
        value = null
    }
}
