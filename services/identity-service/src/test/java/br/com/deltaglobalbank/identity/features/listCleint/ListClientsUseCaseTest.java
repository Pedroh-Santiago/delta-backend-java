package br.com.deltaglobalbank.identity.features.listCleint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.features.apiClients.listClients.ListClientsResponse;
import br.com.deltaglobalbank.identity.features.apiClients.listClients.ListClientsUseCase;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.ApiKeyActiveCount;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ListClientsUseCaseTest {

    private final JpaApiClientRepository jpaApiClientRepository = mock(JpaApiClientRepository.class);
    private final JpaTenantRepository tenantRepository = mock(JpaTenantRepository.class);
    private final JpaApiClientRoleRepository jpaApiClientRoleRepository = mock(JpaApiClientRoleRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final JpaApiKeyRepository jpaApiKeyRepository = mock(JpaApiKeyRepository.class);

    private ListClientsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListClientsUseCase(
            jpaApiClientRepository, tenantRepository, jpaApiClientRoleRepository, roleRepository, jpaApiKeyRepository);
    }

    @Test
    void shouldMapActiveKeysCountFromActiveKeyCountQuery() {
        UUID tenantId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        ApiClientEntity client = mock(ApiClientEntity.class);
        when(client.getId()).thenReturn(clientId);
        when(client.getTenantId()).thenReturn(tenantId);
        when(client.getName()).thenReturn("ERP");
        when(client.getDescription()).thenReturn(null);
        when(client.getStatus()).thenReturn("active");
        when(client.getCreatedAt()).thenReturn(Instant.now());
        when(jpaApiClientRepository.findByTenantId(any(), any()))
            .thenReturn(new PageImpl<>(List.of(client), PageRequest.of(0, 20), 1));

        TenantEntity tenant = mock(TenantEntity.class);
        when(tenant.getSlug()).thenReturn("empresa");
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        when(jpaApiClientRoleRepository.findAllByApiClientIdIn(any())).thenReturn(List.of());

        ApiKeyActiveCount count = mock(ApiKeyActiveCount.class);
        when(count.getApiClientId()).thenReturn(clientId);
        when(count.getTotal()).thenReturn(2L);
        when(jpaApiKeyRepository.countActiveByApiClientIdIn(any(), any())).thenReturn(List.of(count));

        ListClientsResponse response = useCase.listClients(tenantId, 0, 20);

        verify(jpaApiKeyRepository, times(1)).countActiveByApiClientIdIn(any(), any());

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).activeKeysCount());
        assertEquals("empresa", response.items().get(0).tenantSlug());
    }

    @Test
    void shouldMapPaginationMetadataFromPage() {
        UUID tenantId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        ApiClientEntity client = mock(ApiClientEntity.class);
        when(client.getId()).thenReturn(clientId);
        when(client.getTenantId()).thenReturn(tenantId);
        when(client.getName()).thenReturn("ERP");
        when(client.getDescription()).thenReturn(null);
        when(client.getStatus()).thenReturn("active");
        when(client.getCreatedAt()).thenReturn(Instant.now());

        when(jpaApiClientRepository.findByTenantId(any(), any()))
            .thenReturn(new PageImpl<>(List.of(client), PageRequest.of(0, 5), 12));

        TenantEntity tenant = mock(TenantEntity.class);
        when(tenant.getSlug()).thenReturn("empresa");
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(jpaApiClientRoleRepository.findAllByApiClientIdIn(any())).thenReturn(List.of());
        when(jpaApiKeyRepository.countActiveByApiClientIdIn(any(), any())).thenReturn(List.of());

        ListClientsResponse response = useCase.listClients(tenantId, 2, 5);

        assertEquals(2, response.page());
        assertEquals(5, response.size());
        assertEquals(12, response.totalElements());
        assertEquals(3, response.totalPages());
    }
}
