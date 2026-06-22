package de.servicehealth.poppmodule.sdk.federation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FederationDtoTest {
    @Test
    fun entityStatement_idpListEndpoint_is_parsed() {
        val json =
            """
            {
              "metadata": {
                "federation_master": {
                  "idp_list_endpoint": "https://fed.example/idp_list"
                }
              }
            }
            """.trimIndent()
        val statement = FederationJson.instance.decodeFromString<FederationEntityStatement>(json)
        assertEquals("https://fed.example/idp_list", statement.metadata.federationMaster?.idpListEndpoint)
    }

    @Test
    fun entityStatement_missing_federation_master_yields_null() {
        val json = """{"metadata": {}}"""
        val statement = FederationJson.instance.decodeFromString<FederationEntityStatement>(json)
        assertNull(statement.metadata.federationMaster)
    }

    @Test
    fun entityStatement_ignores_unknown_top_level_and_nested_keys() {
        val json =
            """
            {
              "iss": "https://fed.example",
              "exp": 9999999999,
              "metadata": {
                "federation_master": {
                  "idp_list_endpoint": "https://fed.example/idp_list",
                  "future_field": "ignored"
                }
              }
            }
            """.trimIndent()
        val statement = FederationJson.instance.decodeFromString<FederationEntityStatement>(json)
        assertEquals("https://fed.example/idp_list", statement.metadata.federationMaster?.idpListEndpoint)
    }

    @Test
    fun idpListPayload_maps_all_entries_including_optional_logoUri() {
        val json =
            """
            {
              "idp_entity": [
                {
                  "iss": "https://idp1.example",
                  "organization_name": "Test Krankenkasse",
                  "logo_uri": "https://idp1.example/logo.svg"
                },
                {
                  "iss": "https://idp2.example",
                  "organization_name": "Andere Kasse"
                }
              ]
            }
            """.trimIndent()
        val payload = FederationJson.instance.decodeFromString<FederationIdpListPayload>(json)
        assertEquals(2, payload.idpEntities.size)
        assertEquals("https://idp1.example", payload.idpEntities[0].iss)
        assertEquals("Test Krankenkasse", payload.idpEntities[0].organizationName)
        assertEquals("https://idp1.example/logo.svg", payload.idpEntities[0].logoUri)
        assertNull(payload.idpEntities[1].logoUri)
    }

    @Test
    fun federationIdp_survives_json_roundtrip() {
        val idp =
            FederationIdp(
                entityId = "https://idp.example",
                name = "My Kasse",
                logoUri = "https://idp.example/logo.svg",
            )
        val json = FederationJson.instance.encodeToString(FederationIdp.serializer(), idp)
        val decoded = FederationJson.instance.decodeFromString<FederationIdp>(json)
        assertEquals(idp, decoded)
    }
}
