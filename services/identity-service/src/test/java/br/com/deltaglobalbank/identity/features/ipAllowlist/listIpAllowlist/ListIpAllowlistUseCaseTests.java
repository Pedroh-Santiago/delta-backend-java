package br.com.deltaglobalbank.identity.features.ipAllowlist.listIpAllowlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist.ListTenantIpAllowlistQuery;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist.ListTenantIpAllowlistResponse;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist.ListTenantIpAllowlistUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListIpAllowlistUseCaseTests {

    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final TenantIpAllowlistRepository tenantIpAllowlistRepository = mock(TenantIpAllowlistRepository.class);

    private ListTenantIpAllowlistUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListTenantIpAllowlistUseCase(tenantIpAllowlistRepository, tenantRepository);
    }

    @Test
    void mustThrowTenantNotFoundExceptionWhenTenantDoesNotExist() {
        ListTenantIpAllowlistQuery query = new ListTenantIpAllowlistQuery(UUID.randomUUID());
        when(tenantRepository.findById(any())).thenReturn(null);

        assertThrows(TenantNotFoundException.class, () -> useCase.execute(query));
        verify(tenantIpAllowlistRepository, times(0)).findAllByTenantId(any());
    }

    @Test
    void mustReturnEmptyListWhenTenantHasNoEntries() {
        UUID tenantId = UUID.randomUUID();
        ListTenantIpAllowlistQuery query = new ListTenantIpAllowlistQuery(tenantId);

        when(tenantRepository.findById(tenantId)).thenReturn(mock(Tenant.class));
        when(tenantIpAllowlistRepository.findAllByTenantId(tenantId)).thenReturn(List.of());

        ListTenantIpAllowlistResponse response = useCase.execute(query);

        assertEquals(List.of(), response.items());
    }

    @Test
    void mustMapAllEntriesToResponseItems() {
        UUID tenantId = UUID.randomUUID();
        ListTenantIpAllowlistQuery query = new ListTenantIpAllowlistQuery(tenantId);

        when(tenantRepository.findById(tenantId)).thenReturn(mock(Tenant.class));

        TenantIpAllowlist ip1 = mock(TenantIpAllowlist.class);
        when(ip1.getId()).thenReturn(UUID.randomUUID());
        when(ip1.getTenantId()).thenReturn(tenantId);
        when(ip1.getCidr()).thenReturn(new Cidr("192.168.0.0/24"));
        when(ip1.getDescription()).thenReturn("rede 1");
        when(ip1.getCreatedAt()).thenReturn(Instant.now());

        TenantIpAllowlist ip2 = mock(TenantIpAllowlist.class);
        when(ip2.getId()).thenReturn(UUID.randomUUID());
        when(ip2.getTenantId()).thenReturn(tenantId);
        when(ip2.getCidr()).thenReturn(new Cidr("10.0.0.0/8"));
        when(ip2.getDescription()).thenReturn(null);
        when(ip2.getCreatedAt()).thenReturn(Instant.now());

        when(tenantIpAllowlistRepository.findAllByTenantId(tenantId)).thenReturn(List.of(ip1, ip2));

        ListTenantIpAllowlistResponse response = useCase.execute(query);

        Assertions.assertAll(
            () -> assertEquals(2, response.items().size()),
            () -> assertEquals("192.168.0.0/24", response.items().get(0).cidr().value()),
            () -> assertEquals("rede 1", response.items().get(0).description()),
            () -> assertEquals(null, response.items().get(1).description())
        );
    }
}
