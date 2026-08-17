package br.com.deltaglobalbank.identity.features.ipAllowlist.deleteIpAllowlist;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistEntryNotFoundException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist.DeleteTenantIpAllowlistCommand;
import br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist.DeleteTenantIpAllowlistUseCase;
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteIpAllowlistUseCaseTests {

    private final TenantIpAllowlistRepository tenantIpAllowlistRepository = mock(TenantIpAllowlistRepository.class);
    private final TenantIpAllowlistCache tenantIpAllowlistCache = mock(TenantIpAllowlistCache.class);

    private DeleteTenantIpAllowlistUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteTenantIpAllowlistUseCase(tenantIpAllowlistRepository, tenantIpAllowlistCache);
    }

    @Test
    void mustThrowWhenEntryDoesNotExist() {
        DeleteTenantIpAllowlistCommand command = new DeleteTenantIpAllowlistCommand(UUID.randomUUID(), UUID.randomUUID());
        when(tenantIpAllowlistRepository.findById(command.id())).thenReturn(null);

        assertThrows(IpAllowlistEntryNotFoundException.class, () -> useCase.execute(command));
        verify(tenantIpAllowlistRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowWhenEntryBelongsToADifferentTenant() {
        UUID entryId = UUID.randomUUID();
        UUID ownerTenantId = UUID.randomUUID();
        UUID attackerTenantId = UUID.randomUUID();
        DeleteTenantIpAllowlistCommand command = new DeleteTenantIpAllowlistCommand(entryId, attackerTenantId);

        TenantIpAllowlist entry = mock(TenantIpAllowlist.class);
        when(entry.getTenantId()).thenReturn(ownerTenantId);
        when(tenantIpAllowlistRepository.findById(entryId)).thenReturn(entry);

        assertThrows(IpAllowlistEntryNotFoundException.class, () -> useCase.execute(command));
        verify(tenantIpAllowlistRepository, times(0)).delete(any());
    }

    @Test
    void mustDeleteEntryWhenItExistsAndBelongsToTheTenant() {
        UUID entryId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        DeleteTenantIpAllowlistCommand command = new DeleteTenantIpAllowlistCommand(entryId, tenantId);

        TenantIpAllowlist entry = mock(TenantIpAllowlist.class);
        when(entry.getTenantId()).thenReturn(tenantId);
        when(tenantIpAllowlistRepository.findById(entryId)).thenReturn(entry);
        doNothing().when(tenantIpAllowlistRepository).delete(entryId);
        doNothing().when(tenantIpAllowlistCache).invalidate(tenantId);

        useCase.execute(command);

        verify(tenantIpAllowlistRepository, times(1)).delete(entryId);
        verify(tenantIpAllowlistCache, times(1)).invalidate(tenantId);
    }
}
