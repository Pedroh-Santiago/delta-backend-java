package br.com.deltaglobalbank.identity.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiClientTests {

    @Test
    void mustInitializeWithActiveStatus() {
        ApiClient apiClient = ApiClient.newApiClient(UUID.randomUUID(), UUID.randomUUID(), "client", null);
        assertEquals(ApiClientStatus.ACTIVE, apiClient.snapshot().status());
    }

    @Test
    void mustBeEqualWhenIdsMatch() {
        UUID id = UUID.randomUUID();

        ApiClient apiClientA = ApiClient.newApiClient(id, UUID.randomUUID(), "ERP client", null);
        ApiClient apiClientB = ApiClient.newApiClient(id, UUID.randomUUID(), "CRM client", "CRM");
        ApiClient apiClientC = ApiClient.newApiClient(UUID.randomUUID(), UUID.randomUUID(), "Integration client", "Integration client");

        Assertions.assertAll(
            () -> assertEquals(apiClientA, apiClientB),
            () -> assertNotEquals(apiClientA, apiClientC)
        );
    }
}
