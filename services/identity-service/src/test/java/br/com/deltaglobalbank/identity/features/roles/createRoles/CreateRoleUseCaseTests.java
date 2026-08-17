package br.com.deltaglobalbank.identity.features.roles.createRoles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleCodeAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateRoleUseCaseTests {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);

    private CreateRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateRoleUseCase(roleRepository, moduleRepository);
    }

    @Test
    void mustCreateRoleWithoutModule() {
        CreateRoleCommand command = new CreateRoleCommand(
            "internal-payroll.admin", "Administrador da Folha Interna", null, "Payroll admin");

        when(roleRepository.existsByCode(new RoleCode("internal-payroll.admin"))).thenReturn(false);
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRoleResponse response = useCase.execute(command);

        assertEquals("internal-payroll.admin", response.code());
        assertEquals("Administrador da Folha Interna", response.label());
        assertTrue(response.active());
        assertNull(response.moduleId());
        verify(roleRepository, times(1)).save(any());
        verify(moduleRepository, times(0)).findById(any());
    }

    @Test
    void mustNormalizeCodeToLowercase() {
        CreateRoleCommand command = new CreateRoleCommand(
            "Internal-Payroll.Admin", "Administrador da Folha Interna", null, null);

        when(roleRepository.existsByCode(new RoleCode("internal-payroll.admin"))).thenReturn(false);
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRoleResponse response = useCase.execute(command);

        assertEquals("internal-payroll.admin", response.code());
    }

    @Test
    void mustCreateRoleWithValidModule() {
        UUID moduleId = UUID.randomUUID();
        CreateRoleCommand command = new CreateRoleCommand("customers.custom", "Custom Customers", moduleId, null);
        Module module = new Module(moduleId, new ModuleCode("customers"), "Customers", null, Instant.now());

        when(roleRepository.existsByCode(new RoleCode("customers.custom"))).thenReturn(false);
        when(moduleRepository.findById(moduleId)).thenReturn(module);
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRoleResponse response = useCase.execute(command);

        assertEquals(moduleId, response.moduleId());
    }

    @Test
    void mustThrowRoleCodeAlreadyExistsExceptionWhenCodeAlreadyTaken() {
        CreateRoleCommand command = new CreateRoleCommand("platform.admin", "Duplicada", null, null);
        when(roleRepository.existsByCode(new RoleCode("platform.admin"))).thenReturn(true);

        assertThrows(RoleCodeAlreadyExistsException.class, () -> useCase.execute(command));
        verify(roleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowModuleNotFoundExceptionWhenModuleIdDoesNotExist() {
        UUID moduleId = UUID.randomUUID();
        CreateRoleCommand command = new CreateRoleCommand("internal-payroll.admin", "Payroll", moduleId, null);

        when(roleRepository.existsByCode(new RoleCode("internal-payroll.admin"))).thenReturn(false);
        when(moduleRepository.findById(moduleId)).thenReturn(null);

        assertThrows(ModuleNotFoundException.class, () -> useCase.execute(command));
        verify(roleRepository, times(0)).save(any());
    }
}
