package de.servicehealth.poppmodule.sdk.federation

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FederationMasterClientCacheTest {
    private val entityStatementJson =
        """
        {
          "metadata": {
            "federation_master": {
              "idp_list_endpoint": "https://fed.example/idp_list"
            }
          }
        }
        """.trimIndent()

    private val idpListJson =
        """
        {
          "idp_entity": [
            { "iss": "https://idp.example", "organization_name": "Test Kasse" }
          ]
        }
        """.trimIndent()

    private fun countingClient(requestCount: () -> Unit): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestCount()
                    when {
                        request.url.encodedPath.endsWith("openid-federation") ->
                            respond(buildJwt(entityStatementJson), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/plain"))
                        request.url.encodedPath.endsWith("idp_list") ->
                            respond(buildJwt(idpListJson), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/plain"))
                        else -> error("Unexpected request: ${request.url}")
                    }
                }
            }
        }

    @Test
    fun second_fetchIdpList_call_hits_no_additional_network_requests() =
        runTest {
            var count = 0
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = countingClient { count++ },
                )
            client.fetchIdpList()
            client.fetchIdpList()
            client.close()
            // First call: 2 requests (entity statement + idp list). Second call: 0 (cache hit).
            assertEquals(2, count, "Expected exactly 2 HTTP requests total, got $count")
        }

    @Test
    fun forceRefresh_true_re_fetches_from_network() =
        runTest {
            var count = 0
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = countingClient { count++ },
                )
            client.fetchIdpList()
            client.fetchIdpList(forceRefresh = true)
            client.close()
            // Each fetch makes 2 requests → total 4.
            assertEquals(4, count, "Expected 4 HTTP requests (2 per fetch), got $count")
        }

    @Test
    fun concurrent_forceRefresh_calls_deduplicate_to_one_network_fetch() =
        runTest {
            var count = 0
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = countingClient { count++ },
                )
            // Prime the cache so both concurrent callers have the same stale snapshot.
            client.fetchIdpList()
            val results =
                listOf(
                    async { client.fetchIdpList(forceRefresh = true) },
                    async { client.fetchIdpList(forceRefresh = true) },
                ).awaitAll()
            client.close()
            // Initial fill (2) + exactly one force-refresh batch (2) = 4 total.
            assertEquals(4, count, "Expected 4 HTTP requests (initial + one refresh), got $count")
            assertEquals(results[0], results[1], "Both concurrent callers must receive the same list")
        }
}
