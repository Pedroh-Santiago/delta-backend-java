package br.com.deltaglobalbank.identity.features.ipAllowlist.createIpAllowlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.CidrAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.InvalidCidrException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist.CreateTenantIpAllowlistCommand;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist.CreateTenantIpAllowlistResponse;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist.CreateTenantIpAllowlistUsecase;
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateIpAllowlistUseCaseTests {

    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final TenantIpAllowlistRepository tenantIpAllowlistRepository = mock(TenantIpAllowlistRepository.class);
    private final TenantIpAllowlistCache tenantIpAllowlistCache = mock(TenantIpAllowlistCache.class);

    private CreateTenantIpAllowlistUsecase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateTenantIpAllowlistUsecase(tenantIpAllowlistRepository, tenantRepository, tenantIpAllowlistCache);
    }

    @Test
    void mustCreateIpAllowlistSuccessfully() {
        UUID tenantId = UUID.randomUUID();
        CreateTenantIpAllowlistCommand command = new CreateTenantIpAllowlistCommand(tenantId, "192.168.0.0/24", "allowed network");

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        when(tenantIpAllowlistRepository.existsByCidrAndTenantId(any(), org.mockito.ArgumentMatchers.eq(tenantId))).thenReturn(false);

        when(tenantIpAllowlistRepository.save(any())).thenReturn(mock(TenantIpAllowlist.class));
        doNothing().when(tenantIpAllowlistCache).invalidate(any());

        CreateTenantIpAllowlistResponse response = useCase.execute(command);

        Assertions.assertAll(
            () -> assertEquals(tenantId, response.tenantId()),
            () -> assertEquals("allowed network", response.description())
        );
        verify(tenantIpAllowlistRepository, times(1)).save(any());
    }

    @Test
    void mustThrowInvalidCidrExceptionWhenCidrIsInvalid() {
        CreateTenantIpAllowlistCommand command = new CreateTenantIpAllowlistCommand(UUID.randomUUID(), "not.a.cidr/24", null);
        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(any())).thenReturn(tenant);

        assertThrows(InvalidCidrException.class, () -> useCase.execute(command));

        verify(tenantIpAllowlistRepository, times(0)).save(any());
    }

    @Test
    void mustDetectDuplicateCidrAfterNormalization() {
        UUID tenantId = UUID.randomUUID();
        CreateTenantIpAllowlistCommand command = new CreateTenantIpAllowlistCommand(tenantId, "192.168.0.5/24", null);
        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        when(tenantIpAllowlistRepository.existsByCidrAndTenantId("192.168.0.0/24", tenantId)).thenReturn(true);

        assertThrows(CidrAlreadyExistsException.class, () -> useCase.execute(command));
        verify(tenantIpAllowlistRepository, times(0)).save(any());
    }

    @Test
    void mustInvalidateCacheAfterCreatingAllowlistEntry() {
        UUID tenantId = UUID.randomUUID();
        CreateTenantIpAllowlistCommand command = new CreateTenantIpAllowlistCommand(tenantId, "192.168.0.0/24", null);
        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(tenantIpAllowlistRepository.existsByCidrAndTenantId(any(), org.mockito.ArgumentMatchers.eq(tenantId))).thenReturn(false);
        when(tenantIpAllowlistRepository.save(any())).thenReturn(mock(TenantIpAllowlist.class));
        doNothing().when(tenantIpAllowlistCache).invalidate(tenantId);

        useCase.execute(command);

        verify(tenantIpAllowlistCache, times(1)).invalidate(tenantId);
    }
}
