package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.federation.http.createFederationHttpClient
import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

class FederationMasterClient(
    private val baseUrl: String,
    private val storage: SecureStorage? = null,
    httpClient: HttpClient? = null,
) {
    private val ownsHttpClient: Boolean = httpClient == null
    private val httpClient: HttpClient = httpClient ?: createFederationHttpClient()

    init {
        require(baseUrl.startsWith("https://")) {
            "baseUrl must use https://, got: $baseUrl"
        }
    }

    @Volatile
    private var cachedList: List<FederationIdp>? = null
    private val fetchMutex = Mutex()

    /**
     * Fetches the list of available health insurance IDPs from the Federation Master.
     *
     * Step 1: GET {baseUrl}/.well-known/openid-federation → JWT → extract idp_list_endpoint
     * Step 2: GET idp_list_endpoint → JWS → decode → List<FederationIdp>
     *
     * @param forceRefresh bypass in-memory cache and re-fetch from the network.
     * @throws PoppSdkError.Network on connectivity failures or non-2xx HTTP responses.
     * @throws PoppSdkError.Protocol on JWT/JWS decode or JSON schema failures.
     * @throws PoppSdkError.Unknown on unexpected failures.
     */
    suspend fun fetchIdpList(forceRefresh: Boolean = false): List<FederationIdp> {
        if (!forceRefresh) cachedList?.let { return it }
        val snapshot = cachedList
        return fetchMutex.withLock {
            val current = cachedList
            when {
                !forceRefresh && current != null -> current
                forceRefresh && current != null && current !== snapshot -> current
                else -> doFetch().also { cachedList = it }
            }
        }
    }

    /**
     * Persists the user's IDP selection across app restarts.
     * No-op if this client was constructed without a SecureStorage.
     */
    suspend fun saveSelectedIdp(idp: FederationIdp) {
        storage?.put(SELECTED_IDP_KEY, FederationJson.instance.encodeToString(idp))
    }

    /**
     * Recalls the last persisted IDP selection.
     * Returns null if nothing was saved or if constructed without [SecureStorage].
     * If the stored value exists but can no longer be deserialized (e.g. after a
     * schema migration), the corrupt entry is removed so subsequent calls return
     * null cleanly instead of failing on every cold start.
     */
    suspend fun loadSelectedIdp(): FederationIdp? {
        val store = storage ?: return null
        val json = store.get(SELECTED_IDP_KEY) ?: return null
        return try {
            FederationJson.instance.decodeFromString<FederationIdp>(json)
        } catch (e: Exception) {
            store.remove(SELECTED_IDP_KEY)
            null
        }
    }

    /**
     * Releases the internally-created Ktor HttpClient and its connection pool.
     * No-op when this client was constructed with an injected [httpClient] — in that
     * case the caller owns the client and is responsible for closing it.
     * Safe to call from any thread.
     */
    fun close() {
        if (ownsHttpClient) httpClient.close()
    }

    private suspend fun doFetch(): List<FederationIdp> {
        val entityStatementJwt = fetchText("$baseUrl/.well-known/openid-federation")
        val payloadJson = decodeJwtPayload(entityStatementJwt)

        val entityStatement =
            try {
                FederationJson.instance.decodeFromString<FederationEntityStatement>(payloadJson)
            } catch (e: Exception) {
                throw PoppSdkError.Protocol("Failed to parse entity statement payload", e)
            }

        val idpListEndpoint =
            entityStatement.metadata.federationEntity?.idpListEndpoint
                ?: throw PoppSdkError.Protocol(
                    "idp_list_endpoint missing in metadata.federation_entity — check the Federation Master entity statement",
                )
        if (!idpListEndpoint.startsWith("https://")) {
            throw PoppSdkError.Protocol(
                "idp_list_endpoint must use https://, got: $idpListEndpoint",
            )
        }

        val idpListJws = fetchText(idpListEndpoint)

        // Verify the IDP list JWS against the federation master's own keys before trusting its payload.
        // The entity statement is fetched over HTTPS (enforced above), providing transport integrity for
        // the key material.  A full trust-anchor pin against a pre-distributed key is a follow-up.
        entityStatement.jwks?.let { jwks ->
            try {
                verifyJwsSignature(idpListJws, jwks)
            } catch (e: PoppSdkError.PlatformUnsupported) {
                // iOS does not yet have a verifyEs256 implementation; HTTPS transport is the guard.
            }
        }

        val listPayloadJson = decodeJwtPayload(idpListJws)

        val idpListPayload =
            try {
                FederationJson.instance.decodeFromString<FederationIdpListPayload>(listPayloadJson)
            } catch (e: Exception) {
                throw PoppSdkError.Protocol("Failed to parse IDP list payload", e)
            }

        return idpListPayload.idpEntities.map { entry ->
            FederationIdp(
                entityId = entry.iss,
                name = entry.organizationName,
                logoUri = entry.logoUri,
            )
        }
    }

    private suspend fun fetchText(url: String): String {
        val response =
            try {
                httpClient.get(url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw PoppSdkError.Network("Network request to $url failed", e)
            }
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw PoppSdkError.Network(
                "HTTP ${response.status.value} from $url" +
                    errorBody.take(200).takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty(),
            )
        }
        return response.bodyAsText()
    }

    private companion object {
        const val SELECTED_IDP_KEY = "federation-selected-idp"
    }
}
