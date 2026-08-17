package br.com.deltaglobalbank.identity.features.users.removeRoleCode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.user.CannotRemoveOwnAdminRoleException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.features.users.removeUserRole.RemoveUserRoleCommand;
import br.com.deltaglobalbank.identity.features.users.removeUserRole.RemoveUserRoleUseCase;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveUserRoleUseCaseTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final JpaUserRoleRepository userRoleRepository = mock(JpaUserRoleRepository.class);

    private RemoveUserRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RemoveUserRoleUseCase(userRepository, roleRepository, userRoleRepository);
    }

    @Test
    void mustThrowWhenRemovingOwnAdminRole() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            userId, tenantId, "user", List.of(), List.of(), false, UUID.randomUUID());

        RemoveUserRoleCommand command = new RemoveUserRoleCommand(tenantId, userId, new RoleCode("platform.admin"), principal);
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        Assertions.assertThrows(CannotRemoveOwnAdminRoleException.class, () -> useCase.execute(command));
        verify(userRoleRepository, times(0)).delete(any());
    }

    @Test
    void mustAllowRemovingOwnNonAdminRole() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            userId, tenantId, "user", List.of(), List.of(), false, UUID.randomUUID());
        RemoveUserRoleCommand command = new RemoveUserRoleCommand(tenantId, userId, new RoleCode("customers.viewer"), principal);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(roleRepository.findByCode(new RoleCode("customers.viewer"))).thenReturn(role);

        UserRoleEntity entry = mock(UserRoleEntity.class);
        when(userRoleRepository.findByUserIdAndRoleId(userId, roleId)).thenReturn(entry);
        doNothing().when(userRoleRepository).delete(entry);

        useCase.execute(command);

        verify(userRoleRepository, times(1)).delete(entry);
    }

    @Test
    void mustAllowRemovingAdminRoleFromAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of(), List.of(), false, UUID.randomUUID());

        RemoveUserRoleCommand command = new RemoveUserRoleCommand(tenantId, userId, new RoleCode("platform.admin"), principal);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(roleRepository.findByCode(new RoleCode("platform.admin"))).thenReturn(role);

        UserRoleEntity entry = mock(UserRoleEntity.class);
        when(userRoleRepository.findByUserIdAndRoleId(userId, roleId)).thenReturn(entry);
        doNothing().when(userRoleRepository).delete(entry);

        useCase.execute(command);

        verify(userRoleRepository, times(1)).delete(entry);
    }

    @Test
    void mustDoNothingWhenUserDoesNotHaveTheRole() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of(), List.of(), false, UUID.randomUUID());

        RemoveUserRoleCommand command = new RemoveUserRoleCommand(tenantId, userId, new RoleCode("customers.viewer"), principal);

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        Role role = mock(Role.class);
        when(role.getId()).thenReturn(roleId);
        when(roleRepository.findByCode(new RoleCode("customers.viewer"))).thenReturn(role);

        when(userRoleRepository.findByUserIdAndRoleId(userId, roleId)).thenReturn(null);

        useCase.execute(command);

        verify(userRoleRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowUserNotFoundWhenUserDoesNotExist() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), UUID.randomUUID(), "user", List.of(), List.of(), false, UUID.randomUUID());
        RemoveUserRoleCommand command = new RemoveUserRoleCommand(
            UUID.randomUUID(), UUID.randomUUID(), new RoleCode("customers.viewer"), principal);
        when(userRepository.findById(command.userId())).thenReturn(null);

        Assertions.assertThrows(UserNotFound.class, () -> useCase.execute(command));
        verify(userRoleRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowUserNotFoundWhenUserBelongsToADifferentTenant() {
        UUID userId = UUID.randomUUID();
        UUID ownerTenantId = UUID.randomUUID();
        UUID attackerTenantId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), attackerTenantId, "user", List.of(), List.of(), false, UUID.randomUUID());
        RemoveUserRoleCommand command = new RemoveUserRoleCommand(attackerTenantId, userId, new RoleCode("customers.viewer"), principal);

        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(ownerTenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        Assertions.assertThrows(UserNotFound.class, () -> useCase.execute(command));
        verify(userRoleRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            UUID.randomUUID(), tenantId, "user", List.of(), List.of(), false, UUID.randomUUID());

        RemoveUserRoleCommand command = new RemoveUserRoleCommand(tenantId, userId, new RoleCode("nonexistent.role"), principal);

        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);

        when(roleRepository.findByCode(new RoleCode("nonexistent.role"))).thenReturn(null);

        Assertions.assertThrows(RoleNotFoundException.class, () -> useCase.execute(command));
        verify(userRoleRepository, times(0)).delete(any());
    }
}
