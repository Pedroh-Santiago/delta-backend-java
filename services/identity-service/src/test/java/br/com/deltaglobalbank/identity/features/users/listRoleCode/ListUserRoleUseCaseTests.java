package br.com.deltaglobalbank.identity.features.users.listRoleCode;

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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.features.users.listUserRoles.ListUserRoleQuery;
import br.com.deltaglobalbank.identity.features.users.listUserRoles.ListUserRoleUseCase;
import br.com.deltaglobalbank.identity.features.users.listUserRoles.ListUserRolesResponse;
import br.com.deltaglobalbank.identity.features.users.listUserRoles.ListedUserRoles;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListUserRoleUseCaseTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final JpaUserRoleRepository userRoleRepository = mock(JpaUserRoleRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);

    private ListUserRoleUseCase useCase;

    private UUID userId;
    private UUID tenantId;
    private UUID roleId;
    private UUID moduleId;

    @BeforeEach
    void setUp() {
        useCase = new ListUserRoleUseCase(userRepository, userRoleRepository, roleRepository, moduleRepository);
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        moduleId = UUID.randomUUID();
    }

    @Test
    void mustReturnEmptyListWhenUserHasNoRoles() {
        ListUserRoleQuery query = new ListUserRoleQuery(tenantId, userId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of());

        ListUserRolesResponse response = useCase.execute(query);

        assertTrue(response.items().isEmpty());

        verify(roleRepository, times(0)).findAllByIds(any());
        verify(moduleRepository, times(0)).findAllByIds(any());
    }

    @Test
    void mustAssembleRoleWithItsModuleCode() {
        ListUserRoleQuery query = new ListUserRoleQuery(tenantId, userId);
        Instant grantedAtInstant = Instant.now();
        UUID grantedByUserId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        UserRoleEntity entry = mock(UserRoleEntity.class);
        when(entry.getRoleId()).thenReturn(roleId);
        when(entry.getGrantedAt()).thenReturn(grantedAtInstant);
        when(entry.getGrantedBy()).thenReturn(grantedByUserId);
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(entry));

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(role.getCode()).thenReturn(new RoleCode("customers.admin"));
        when(role.getModuleId()).thenReturn(moduleId);
        when(roleRepository.findAllByIds(Set.of(roleId))).thenReturn(List.of(role));

        Module module = mock(Module.class);
        when(module.getId()).thenReturn(moduleId);
        when(module.getCode()).thenReturn(new ModuleCode("customers"));
        when(moduleRepository.findAllByIds(Set.of(moduleId))).thenReturn(List.of(module));

        ListUserRolesResponse response = useCase.execute(query);

        ListedUserRoles item = response.items().get(0);
        Assertions.assertAll(
            () -> assertEquals("customers.admin", item.roleCode()),
            () -> assertEquals(roleId, item.roleId()),
            () -> assertEquals("customers", item.moduleCode()),
            () -> assertEquals(grantedAtInstant, item.grantedAt()),
            () -> assertEquals(grantedByUserId, item.grantedBy())
        );
    }

    @Test
    void mustListRoleWithNullModuleCodeWhenRoleHasNoModule() {
        ListUserRoleQuery query = new ListUserRoleQuery(tenantId, userId);
        UUID grantedByUserId = UUID.randomUUID();

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        UserRoleEntity entry = mock(UserRoleEntity.class);
        when(entry.getRoleId()).thenReturn(roleId);
        when(entry.getGrantedAt()).thenReturn(Instant.now());
        when(entry.getGrantedBy()).thenReturn(grantedByUserId);
        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(entry));

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(role.getCode()).thenReturn(new RoleCode("platform.admin"));
        when(role.getModuleId()).thenReturn(null);
        when(roleRepository.findAllByIds(Set.of(roleId))).thenReturn(List.of(role));

        when(moduleRepository.findAllByIds(Set.of())).thenReturn(List.of());

        ListUserRolesResponse response = useCase.execute(query);

        ListedUserRoles item = response.items().get(0);
        Assertions.assertAll(
            () -> assertEquals("platform.admin", item.roleCode()),
            () -> assertNull(item.moduleCode())
        );
    }

    @Test
    void mustAssembleMultipleRolesWithAndWithoutModules() {
        ListUserRoleQuery query = new ListUserRoleQuery(tenantId, userId);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        UUID roleAId = UUID.randomUUID();
        UUID moduleAId = UUID.randomUUID();
        UserRoleEntity entryA = mock(UserRoleEntity.class);
        when(entryA.getRoleId()).thenReturn(roleAId);
        when(entryA.getGrantedAt()).thenReturn(Instant.now());
        when(entryA.getGrantedBy()).thenReturn(UUID.randomUUID());

        UUID roleBId = UUID.randomUUID();
        UserRoleEntity entryB = mock(UserRoleEntity.class);
        when(entryB.getRoleId()).thenReturn(roleBId);
        when(entryB.getGrantedAt()).thenReturn(Instant.now());
        when(entryB.getGrantedBy()).thenReturn(UUID.randomUUID());

        when(userRoleRepository.findAllByUserId(userId)).thenReturn(List.of(entryA, entryB));

        Role roleA = mock(Role.class);
        when(roleA.getId()).thenReturn(roleAId);
        when(roleA.getCode()).thenReturn(new RoleCode("customers.admin"));
        when(roleA.getModuleId()).thenReturn(moduleAId);
        Role roleB = mock(Role.class);
        when(roleB.getId()).thenReturn(roleBId);
        when(roleB.getCode()).thenReturn(new RoleCode("platform.admin"));
        when(roleB.getModuleId()).thenReturn(null);
        when(roleRepository.findAllByIds(Set.of(roleAId, roleBId))).thenReturn(List.of(roleA, roleB));

        Module moduleA = mock(Module.class);
        when(moduleA.getId()).thenReturn(moduleAId);
        when(moduleA.getCode()).thenReturn(new ModuleCode("customers"));
        when(moduleRepository.findAllByIds(Set.of(moduleAId))).thenReturn(List.of(moduleA));

        ListUserRolesResponse response = useCase.execute(query);

        ListedUserRoles itemA = response.items().stream().filter(it -> it.roleId().equals(roleAId)).findFirst().orElseThrow();
        ListedUserRoles itemB = response.items().stream().filter(it -> it.roleId().equals(roleBId)).findFirst().orElseThrow();
        Assertions.assertAll(
            () -> assertEquals(2, response.items().size()),
            () -> assertEquals("customers.admin", itemA.roleCode()),
            () -> assertEquals("customers", itemA.moduleCode()),
            () -> assertEquals("platform.admin", itemB.roleCode()),
            () -> assertNull(itemB.moduleCode())
        );
    }

    @Test
    void mustThrowUserNotFoundWhenUserBelongsToADifferentTenant() {
        ListUserRoleQuery query = new ListUserRoleQuery(UUID.randomUUID(), userId);
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findById(userId)).thenReturn(user);
        assertThrows(UserNotFound.class, () -> useCase.execute(query));
    }
}
