package br.com.deltaglobalbank.identity.features.userRoles.assignUserRole;

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

import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleInactiveException;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDeniedException;

class AssignUserUseCaseTest {

    private final TenantModuleRepository tenantModuleRepository = mock(TenantModuleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final JpaUserRoleRepository jpaUserRoleRepository = mock(JpaUserRoleRepository.class);

    private AssignRoleUseCase assignUserUseCase;

    @BeforeEach
    void setup() {
        assignUserUseCase = new AssignRoleUseCase(
            tenantModuleRepository, moduleRepository, userRepository, roleRepository, jpaUserRoleRepository);
    }

    @Test
    void shouldThrowUserNotFoundWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID actingTenantId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), actingTenantId, "user", List.of("identity.admin"), List.of(), false, UUID.randomUUID());
        AssignRolesRequest request = new AssignRolesRequest(List.of(new RoleCode("lending.viewer")));

        when(userRepository.findById(userId)).thenReturn(null);

        assertThrows(UserNotFound.class,
            () -> assignUserUseCase.assignRoleTenant(actingTenantId, principal, userId, request));

        verify(jpaUserRoleRepository, times(0)).save(any());
    }

    @Test
    void shouldThrowTenantAccessDeniedExceptionWhenUserBelongsToAnotherTenant() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        UUID otherTenantId = UUID.randomUUID();
        UUID actingTenantId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), actingTenantId, "user", List.of("identity.admin"), List.of(), false, UUID.randomUUID());
        AssignRolesRequest request = new AssignRolesRequest(List.of(new RoleCode("lending.viewer")));

        when(user.getTenantId()).thenReturn(otherTenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        assertThrows(UserNotFound.class,
            () -> assignUserUseCase.assignRoleTenant(actingTenantId, principal, userId, request));

        verify(jpaUserRoleRepository, times(0)).save(any());
    }

    @Test
    void shouldThrowRoleNotFoundExceptionWhenRoleCodeDoesNotExist() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        UUID actingTenantId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), actingTenantId, "user", List.of("identity.admin"), List.of(), false, UUID.randomUUID());
        RoleCode roleCode = new RoleCode("lending.viewer");
        AssignRolesRequest request = new AssignRolesRequest(List.of(roleCode));

        when(roleRepository.findByCode(roleCode)).thenReturn(null);
        when(user.getTenantId()).thenReturn(actingTenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        assertThrows(RoleNotFoundException.class,
            () -> assignUserUseCase.assignRoleTenant(actingTenantId, principal, userId, request));

        verify(jpaUserRoleRepository, times(0)).save(any());
    }

    @Test
    void shouldThrowAuthorizationDeniedExceptionWhenNonPlatformAdminGrantsPlatformAdmin() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actingTenantId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), actingTenantId, "user", List.of("identity.admin"), List.of(), false, UUID.randomUUID());

        RoleCode roleCode = new RoleCode("platform.admin");
        AssignRolesRequest request = new AssignRolesRequest(List.of(roleCode));

        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(null);
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        AuthorizationDeniedException ex = assertThrows(AuthorizationDeniedException.class,
            () -> assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request));

        assertEquals("denied", ex.getMessage());

        verify(jpaUserRoleRepository, times(0)).save(any());
    }

    @Test
    void shouldAssignNewRoleAndReturnAddedRolesWhenAllValidationsPass() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = mock(User.class);
        UUID userIdValue = UUID.randomUUID();
        when(user.getId()).thenReturn(userIdValue);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of("platform.admin"), List.of(), false, UUID.randomUUID());

        UUID moduleId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("lending.viewer");
        AssignRolesRequest request = new AssignRolesRequest(List.of(roleCode));

        Role role = mock(Role.class);
        when(role.getCode()).thenReturn(roleCode);
        when(role.getModuleId()).thenReturn(moduleId);
        when(role.getId()).thenReturn(UUID.randomUUID());
        when(role.isActive()).thenReturn(true);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        when(roleRepository.findAllByUserId(userIdValue)).thenReturn(List.of());

        AssignRolesResponse response = assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request);

        verify(jpaUserRoleRepository, times(1)).save(any());
        assertEquals(userIdValue, response.userId());
        assertEquals(List.of(roleCode), response.addedRoles());
    }

    @Test
    void shouldNotSaveWhenUserAlreadyHasTheRequestedRole() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = mock(User.class);
        UUID userIdValue = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        RoleCode roleCode = new RoleCode("lending.viewer");
        Role existingRole = new Role(UUID.randomUUID(), roleCode, moduleId, null, null, true, Instant.now());

        when(user.getId()).thenReturn(userIdValue);
        when(roleRepository.findByCode(roleCode)).thenReturn(existingRole);
        when(roleRepository.findAllByUserId(userIdValue)).thenReturn(List.of(existingRole));
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of("platform.admin"), List.of(), false, UUID.randomUUID());

        AssignRolesRequest request = new AssignRolesRequest(List.of(new RoleCode("lending.viewer")));

        AssignRolesResponse response = assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request);

        verify(jpaUserRoleRepository, times(0)).save(any());
        assertEquals(List.of(), response.addedRoles());
    }

    @Test
    void shouldAddOnlyTheMissingRoleWhenUserAlreadyHasOneOfTheRequestedRoles() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userIdValue = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        User user = mock(User.class);

        Role existingRole = new Role(
            UUID.randomUUID(), new RoleCode("lending.viewer"), moduleId, null, null, true, Instant.now());
        Role newRole = new Role(
            UUID.randomUUID(), new RoleCode("customers.viewer"), moduleId, null, null, true, Instant.now());

        AssignRolesRequest request = new AssignRolesRequest(List.of(existingRole.getCode(), newRole.getCode()));

        when(user.getId()).thenReturn(userIdValue);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        when(roleRepository.findByCode(existingRole.getCode())).thenReturn(existingRole);
        when(roleRepository.findByCode(newRole.getCode())).thenReturn(newRole);

        when(roleRepository.findAllByUserId(userIdValue)).thenReturn(List.of(existingRole));

        TenantModule tenantModule = mock(TenantModule.class);
        when(tenantModule.getModuleId()).thenReturn(moduleId);
        when(tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true)).thenReturn(List.of(tenantModule));

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of("platform.admin"), List.of(), false, UUID.randomUUID());

        AssignRolesResponse response = assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request);

        verify(jpaUserRoleRepository, times(1)).save(any());
        assertEquals(List.of(newRole.getCode()), response.addedRoles());
    }

    @Test
    void shouldThrowRoleInactiveExceptionWhenRoleIsDeactivated() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of("platform.admin"), List.of(), false, UUID.randomUUID());

        RoleCode roleCode = new RoleCode("lending.viewer");
        AssignRolesRequest request = new AssignRolesRequest(List.of(roleCode));

        Role role = mock(Role.class);
        when(role.isActive()).thenReturn(false);
        when(roleRepository.findByCode(roleCode)).thenReturn(role);

        assertThrows(RoleInactiveException.class,
            () -> assignUserUseCase.assignRoleTenant(tenantId, principal, userId, request));

        verify(jpaUserRoleRepository, times(0)).save(any());
    }
}
