package br.com.deltaglobalbank.identity.features.apiClient.createApiClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.apiClient.ApiClient;
import br.com.deltaglobalbank.identity.domain.apiClient.ApiClientRepository;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleInactiveException;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException;
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException;
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.features.apiClients.createApiClient.CreateApiClientCommand;
import br.com.deltaglobalbank.identity.features.apiClients.createApiClient.CreateApiClientResponse;
import br.com.deltaglobalbank.identity.features.apiClients.createApiClient.CreateApiClientUseCase;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDeniedException;

class CreateApiClientTests {

    private final ApiClientRepository apiClientRepository = mock(ApiClientRepository.class);
    private final TenantRepository tenantRepository = mock(TenantRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final TenantModuleRepository tenantModuleRepository = mock(TenantModuleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final JpaApiClientRoleRepository apiClientRoleRepository = mock(JpaApiClientRoleRepository.class);

    private CreateApiClientUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateApiClientUseCase(
            apiClientRepository, tenantRepository, roleRepository,
            tenantModuleRepository, moduleRepository, apiClientRoleRepository
        );
    }

    @Test
    void mustCreateApiClientSuccessfullyWithRoles() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("customers.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", "ERP", List.of(roleCode), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        UUID moduleId = UUID.randomUUID();
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        when(apiClientRepository.save(any())).thenReturn(mock(ApiClient.class));

        CreateApiClientResponse response = useCase.execute(command);

        Assertions.assertAll(
            () -> assertEquals(tenantId, response.tenantId()),
            () -> assertEquals("ERP-client", response.name()),
            () -> assertEquals("ERP", response.description())
        );

        verify(apiClientRepository, times(1)).save(any());
        verify(apiClientRoleRepository, times(1)).save(any());
    }

    @Test
    void mustThrowTenantNotFoundExceptionWhenTenantDoesNotExist() {
        CreateApiClientCommand command = new CreateApiClientCommand(
            UUID.randomUUID(), "ERP-client", null, List.of(new RoleCode("customers.admin")), List.of("platform.admin"));

        when(tenantRepository.findById(any())).thenReturn(null);

        assertThrows(TenantNotFoundException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowTenantInactiveExceptionWhenTenantIsInactive() {
        UUID tenantId = UUID.randomUUID();
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(new RoleCode("customers.admin")), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        assertThrows(TenantInactiveException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowDuplicateRoleExceptionWhenRoleCodesAreExactlyDuplicated() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("customers.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode, roleCode), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        assertThrows(DuplicateRoleException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowDuplicateRoleExceptionWhenRoleCodesDifferOnlyByCase() {
        UUID tenantId = UUID.randomUUID();
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null,
            List.of(new RoleCode("CUSTOMERS.ADMIN"), new RoleCode("customers.admin")), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        assertThrows(DuplicateRoleException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("nonexistent.role");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        when(roleRepository.findByCode(roleCode)).thenReturn(null);

        assertThrows(RoleNotFoundException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustThrowModuleNotEnabledForTenantExceptionWhenRoleModuleIsNotEnabled() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("customers.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        UUID moduleId = UUID.randomUUID();
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of());
        when(moduleRepository.findById(moduleId)).thenReturn(null);

        assertThrows(ModuleNotEnabledForTenantException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustDenyWhenNonPlatformAdminCreatesApiClientWithPlatformAdminRole() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("platform.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(null);
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        AuthorizationDeniedException ex = assertThrows(AuthorizationDeniedException.class, () -> useCase.execute(command));
        assertEquals("denied", ex.getMessage());

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustAllowPlatformAdminToCreateApiClientWithPlatformAdminRole() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("platform.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode), List.of("platform.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(null);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        when(apiClientRepository.save(any())).thenReturn(mock(ApiClient.class));

        CreateApiClientResponse response = useCase.execute(command);

        assertEquals(tenantId, response.tenantId());
        verify(apiClientRepository, times(1)).save(any());
        verify(apiClientRoleRepository, times(1)).save(any());
    }

    @Test
    void mustDenyPlatformAdminRoleEvenWhenNotFirstInTheList() {
        UUID tenantId = UUID.randomUUID();
        RoleCode commonCode = new RoleCode("customers.viewer");
        RoleCode adminCode = new RoleCode("platform.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(commonCode, adminCode), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Role commonRole = mock(Role.class);
        when(commonRole.getCode()).thenReturn(commonCode);
        when(commonRole.getModuleId()).thenReturn(null);
        when(commonRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(commonCode)).thenReturn(commonRole);

        Role adminRole = mock(Role.class);
        when(adminRole.getCode()).thenReturn(adminCode);
        when(adminRole.getModuleId()).thenReturn(null);
        when(adminRole.isActive()).thenReturn(true);
        when(roleRepository.findByCode(adminCode)).thenReturn(adminRole);

        AuthorizationDeniedException ex = assertThrows(AuthorizationDeniedException.class, () -> useCase.execute(command));
        assertEquals("denied", ex.getMessage());

        verify(apiClientRepository, times(0)).save(any());
        verify(apiClientRoleRepository, times(0)).save(any());
    }

    @Test
    void mustAllowNonPlatformAdminToCreateApiClientWithCommonRole() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("customers.viewer");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        UUID moduleId = UUID.randomUUID();
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        when(apiClientRepository.save(any())).thenReturn(mock(ApiClient.class));

        CreateApiClientResponse response = useCase.execute(command);

        assertEquals(tenantId, response.tenantId());
        verify(apiClientRepository, times(1)).save(any());
        verify(apiClientRoleRepository, times(1)).save(any());
    }

    @Test
    void mustThrowRoleInactiveExceptionWhenRoleIsDeactivated() {
        UUID tenantId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("internal-payroll.admin");
        CreateApiClientCommand command = new CreateApiClientCommand(
            tenantId, "ERP-client", null, List.of(roleCode), List.of("identity.admin"));

        Tenant tenant = mock(Tenant.class);
        when(tenant.isActive()).thenReturn(true);
        when(tenantRepository.findById(tenantId)).thenReturn(tenant);

        Role role = mock(Role.class);
        when(role.isActive()).thenReturn(false);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        assertThrows(RoleInactiveException.class, () -> useCase.execute(command));

        verify(apiClientRepository, times(0)).save(any());
    }
}
