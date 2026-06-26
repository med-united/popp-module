package de.servicehealth.poppmodule.sdk.federation

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Hits the live gematik Reference Environment (RU) Federation Master.
 * Requires internet access.
 *
 * Run with: ./gradlew :popp-sdk:testAndroidHostTest -Pintegration
 *
 * Excluded from normal CI automatically — the testAndroidHostTest Gradle task
 * excludes *IntegrationTest files unless -Pintegration is passed.
 */
class FederationMasterClientIntegrationTest {
    @Test
    fun fetchIdpList_returns_nonempty_list_from_reference_environment() =
        runBlocking {
            val client = FederationMasterClient(baseUrl = "https://app-ref.federationmaster.de")
            try {
                val list = client.fetchIdpList()
                assertTrue(list.isNotEmpty(), "Expected at least one IDP in the RU, got empty list")
                assertTrue(list.all { it.entityId.isNotBlank() }, "Every IDP must have a non-blank entityId")
                assertTrue(list.all { it.name.isNotBlank() }, "Every IDP must have a non-blank name")
            } finally {
                client.close()
            }
        }
}
