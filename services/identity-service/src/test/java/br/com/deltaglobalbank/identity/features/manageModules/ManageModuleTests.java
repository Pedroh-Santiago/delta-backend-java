package br.com.deltaglobalbank.identity.features.manageModules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.tenant.TenantSlug;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.features.modules.disableModule.DisableTenantModuleCommand;
import br.com.deltaglobalbank.identity.features.modules.disableModule.DisableTenantModuleUseCase;
import br.com.deltaglobalbank.identity.features.modules.enableModule.EnableModuleResponse;
import br.com.deltaglobalbank.identity.features.modules.enableModule.EnableTenantModuleCommand;
import br.com.deltaglobalbank.identity.features.modules.enableModule.EnableTenantModuleUseCase;
import br.com.deltaglobalbank.identity.features.modules.listModules.ListTenantModulesResponse;
import br.com.deltaglobalbank.identity.features.modules.listModules.ListTenantModulesUseCase;
import br.com.deltaglobalbank.identity.features.modules.listModules.TenantModuleListItem;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ManageModuleTests {

    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final TenantModuleRepository tenantModuleRepository = mock(TenantModuleRepository.class);

    private final EnableTenantModuleUseCase enableUseCase =
        new EnableTenantModuleUseCase(tenantRepository, moduleRepository, tenantModuleRepository);
    private final DisableTenantModuleUseCase disableUseCase =
        new DisableTenantModuleUseCase(tenantRepository, moduleRepository, tenantModuleRepository);
    private final ListTenantModulesUseCase listUseCase =
        new ListTenantModulesUseCase(tenantRepository, moduleRepository, tenantModuleRepository);

    private final UUID tenantId = UUID.randomUUID();
    private final UUID moduleId = UUID.randomUUID();
    private final UUID cardModuleId = UUID.randomUUID();

    private final Tenant tenant = Tenant.create(tenantId, "Empresa Teste", new TenantSlug("empresa-teste"));

    private final Module lendingModule =
        new Module(moduleId, new ModuleCode("lending"), "Empréstimos", "teste", Instant.now());

    private final Module cardModule =
        new Module(cardModuleId, new ModuleCode("card"), "Cartão", "teste", Instant.now());

    @Test
    void moduleShouldBeEnabled() {
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findByCode(new ModuleCode("lending"))).thenReturn(lendingModule);
        when(tenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId)).thenReturn(null);

        ArgumentCaptor<TenantModule> savedCaptor = ArgumentCaptor.forClass(TenantModule.class);
        when(tenantModuleRepository.save(savedCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        EnableModuleResponse response = enableUseCase.execute(new EnableTenantModuleCommand(tenantId, "lending"));

        assertEquals("lending", response.moduleCode());
        assertTrue(response.enabled());
        assertNotNull(response.enabledAt());
        assertTrue(savedCaptor.getValue().isEnabled());
    }

    @Test
    void moduleShouldBeReactivated() {
        TenantModule existingTenantModule = TenantModule.create(UUID.randomUUID(), tenantId, cardModuleId);
        existingTenantModule.disable();
        assertFalse(existingTenantModule.isEnabled());

        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findByCode(new ModuleCode("card"))).thenReturn(cardModule);
        when(tenantModuleRepository.findByTenantIdAndModuleId(tenantId, cardModuleId)).thenReturn(existingTenantModule);
        when(tenantModuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EnableModuleResponse response = enableUseCase.execute(new EnableTenantModuleCommand(tenantId, "card"));

        assertTrue(response.enabled());
        assertTrue(existingTenantModule.isEnabled());
    }

    @Test
    void shouldReturnModuleNotFoundWhenModuleDoesNotExist() {
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findByCode(new ModuleCode("inexistente"))).thenReturn(null);

        assertThrows(ModuleNotFoundException.class,
            () -> enableUseCase.execute(new EnableTenantModuleCommand(tenantId, "inexistente")));
    }

    @Test
    void shouldReturnTenantNotFoundWhenTenantDoesNotExist() {
        UUID unknownTenantId = UUID.randomUUID();
        when(tenantRepository.findById(unknownTenantId)).thenReturn(null);

        assertThrows(TenantNotFoundException.class,
            () -> enableUseCase.execute(new EnableTenantModuleCommand(unknownTenantId, "lending")));
    }

    @Test
    void moduleShouldBeDisabled() {
        TenantModule existingTenantModule = TenantModule.create(UUID.randomUUID(), tenantId, moduleId);
        assertTrue(existingTenantModule.isEnabled());

        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findByCode(new ModuleCode("lending"))).thenReturn(lendingModule);
        when(tenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId)).thenReturn(existingTenantModule);
        when(tenantModuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        disableUseCase.execute(new DisableTenantModuleCommand(tenantId, "lending"));

        assertFalse(existingTenantModule.isEnabled());
        verify(tenantModuleRepository, times(1)).save(existingTenantModule);
    }

    @Test
    void disablingModuleWithoutEntryIsIdempotent() {
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findByCode(new ModuleCode("card"))).thenReturn(cardModule);
        when(tenantModuleRepository.findByTenantIdAndModuleId(tenantId, cardModuleId)).thenReturn(null);

        disableUseCase.execute(new DisableTenantModuleCommand(tenantId, "card"));

        verify(tenantModuleRepository, times(0)).save(any());
    }

    @Test
    void moduleShouldBeListed() {
        TenantModule lendingTm = TenantModule.create(UUID.randomUUID(), tenantId, moduleId);
        TenantModule cardTm = TenantModule.create(UUID.randomUUID(), tenantId, cardModuleId);
        cardTm.disable();

        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findAll()).thenReturn(List.of(lendingModule, cardModule));
        when(tenantModuleRepository.findAllByTenantId(tenantId)).thenReturn(List.of(lendingTm, cardTm));

        ListTenantModulesResponse response = listUseCase.execute(tenantId);

        assertEquals(2, response.items().size());
        TenantModuleListItem lendingItem = response.items().stream()
            .filter(it -> it.moduleCode().equals("lending")).findFirst().orElseThrow();
        TenantModuleListItem cardItem = response.items().stream()
            .filter(it -> it.moduleCode().equals("card")).findFirst().orElseThrow();
        assertTrue(lendingItem.enabled());
        assertFalse(cardItem.enabled());
    }

    @Test
    void listShouldShowModuleAsDisabledWhenTenantHasNoEntry() {
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);
        when(moduleRepository.findAll()).thenReturn(List.of(lendingModule));
        when(tenantModuleRepository.findAllByTenantId(tenantId)).thenReturn(List.of());

        ListTenantModulesResponse response = listUseCase.execute(tenantId);

        assertEquals(1, response.items().size());
        assertFalse(response.items().get(0).enabled());
    }
}
