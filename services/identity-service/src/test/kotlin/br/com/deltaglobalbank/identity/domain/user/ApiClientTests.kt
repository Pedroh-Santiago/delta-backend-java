package br.com.deltaglobalbank.identity.domain.user

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientStatus
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNotEquals

@ExtendWith(MockKExtension::class)
class ApiClientTests {
    @Test
    fun `must initialize with active status`() {
        val apiClient = ApiClient.newApiClient(
            id = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            name = "client",
            description = null
        )
        assertEquals(ApiClientStatus.ACTIVE, apiClient.snapshot().status)
    }

    @Test
    fun `must be equal when ids match`() {
        val id = UUID.randomUUID()

        val ApiClientA = ApiClient.newApiClient(id = id, tenantId = UUID.randomUUID(), name = "ERP client", description = null)
        val ApiClientB = ApiClient.newApiClient(id = id, tenantId = UUID.randomUUID(), name = "CRM client", description = "CRM")
        val ApiClientC = ApiClient.newApiClient(id = UUID.randomUUID(), tenantId = UUID.randomUUID(), name = "Integration client", description = "Integration client")

        assertAll(
            {assertEquals(ApiClientA, ApiClientB)},
            { assertNotEquals(ApiClientA, ApiClientC) }
        )

    }
}