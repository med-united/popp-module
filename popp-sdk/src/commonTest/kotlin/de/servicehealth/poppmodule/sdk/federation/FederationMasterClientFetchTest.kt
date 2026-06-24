package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.PoppSdkError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FederationMasterClientFetchTest {
    private val entityStatementJson =
        """
        {
          "metadata": {
            "federation_entity": {
              "idp_list_endpoint": "https://fed.example/idp_list"
            }
          }
        }
        """.trimIndent()

    private val idpListJson =
        """
        {
          "idp_entity": [
            {
              "iss": "https://idp1.example",
              "organization_name": "Test Kasse",
              "logo_uri": "https://idp1.example/logo.svg"
            }
          ]
        }
        """.trimIndent()

    private fun mockClient(
        entityStatementBody: String = buildJwt(entityStatementJson),
        idpListBody: String = buildJwt(idpListJson),
        entityStatementStatus: HttpStatusCode = HttpStatusCode.OK,
        idpListStatus: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when {
                        request.url.encodedPath.endsWith("openid-federation") ->
                            respond(entityStatementBody, entityStatementStatus, headersOf(HttpHeaders.ContentType, "text/plain"))
                        request.url.encodedPath.endsWith("idp_list") ->
                            respond(idpListBody, idpListStatus, headersOf(HttpHeaders.ContentType, "text/plain"))
                        else -> error("Unexpected request: ${request.url}")
                    }
                }
            }
        }

    @Test
    fun fetchIdpList_maps_idp_entries_correctly() =
        runTest {
            val client = FederationMasterClient(baseUrl = "https://fed.example", httpClient = mockClient())
            val list = client.fetchIdpList()
            client.close()
            assertEquals(1, list.size)
            assertEquals("https://idp1.example", list[0].entityId)
            assertEquals("Test Kasse", list[0].name)
            assertEquals("https://idp1.example/logo.svg", list[0].logoUri)
        }

    @Test
    fun fetchIdpList_non200_from_entity_statement_throws_network_error() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = mockClient(entityStatementStatus = HttpStatusCode.ServiceUnavailable),
                )
            assertFailsWith<PoppSdkError.Network> { client.fetchIdpList() }
            client.close()
        }

    @Test
    fun fetchIdpList_non200_from_idp_list_throws_network_error() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = mockClient(idpListStatus = HttpStatusCode.NotFound),
                )
            assertFailsWith<PoppSdkError.Network> { client.fetchIdpList() }
            client.close()
        }

    @Test
    fun fetchIdpList_malformed_entity_statement_jwt_throws_protocol_error() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = mockClient(entityStatementBody = "not-a-jwt"),
                )
            assertFailsWith<PoppSdkError.Protocol> { client.fetchIdpList() }
            client.close()
        }

    @Test
    fun fetchIdpList_missing_idpListEndpoint_in_payload_throws_protocol_error() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = mockClient(entityStatementBody = buildJwt("""{"metadata": {}}""")),
                )
            assertFailsWith<PoppSdkError.Protocol> { client.fetchIdpList() }
            client.close()
        }

    @Test
    fun fetchIdpList_http_idpListEndpoint_in_entity_statement_throws_protocol_error() =
        runTest {
            val entityStatementWithHttpEndpoint =
                """
                {
                  "metadata": {
                    "federation_entity": {
                      "idp_list_endpoint": "http://fed.example/idp_list"
                    }
                  }
                }
                """.trimIndent()
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    httpClient = mockClient(entityStatementBody = buildJwt(entityStatementWithHttpEndpoint)),
                )
            assertFailsWith<PoppSdkError.Protocol> { client.fetchIdpList() }
            client.close()
        }

    @Test
    fun constructor_rejects_http_baseUrl() {
        assertFailsWith<IllegalArgumentException> {
            FederationMasterClient(baseUrl = "http://fed.example")
        }
    }
}
