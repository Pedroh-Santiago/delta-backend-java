package br.com.deltaglobalbank.identity.features.roles.deleteRoles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleInUseException;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleProtectedException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteRoleUseCaseTests {

    private final RoleRepository roleRepository = mock(RoleRepository.class);

    private DeleteRoleUseCase useCase;

    private final UUID roleId = UUID.randomUUID();

    private Role role(String code) {
        return new Role(roleId, new RoleCode(code), null, null, null, true, Instant.now());
    }

    @BeforeEach
    void setUp() {
        useCase = new DeleteRoleUseCase(roleRepository);
    }

    @Test
    void mustDeleteRoleWhenFoundNotProtectedAndNotInUse() {
        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin"));
        when(roleRepository.isAssignedToAnyUser(roleId)).thenReturn(false);
        when(roleRepository.isAssignedToAnyApiClient(roleId)).thenReturn(false);
        doNothing().when(roleRepository).delete(roleId);

        useCase.execute(roleId);

        verify(roleRepository, times(1)).delete(roleId);
    }

    @Test
    void mustThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        when(roleRepository.findById(roleId)).thenReturn(null);

        assertThrows(RoleNotFoundException.class, () -> useCase.execute(roleId));
        verify(roleRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowRoleProtectedExceptionWhenRoleIsPlatformAdmin() {
        when(roleRepository.findById(roleId)).thenReturn(role("platform.admin"));

        assertThrows(RoleProtectedException.class, () -> useCase.execute(roleId));
        verify(roleRepository, times(0)).isAssignedToAnyUser(any());
        verify(roleRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowRoleInUseExceptionWhenRoleIsAssignedToAUser() {
        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin"));
        when(roleRepository.isAssignedToAnyUser(roleId)).thenReturn(true);
        when(roleRepository.isAssignedToAnyApiClient(roleId)).thenReturn(false);

        assertThrows(RoleInUseException.class, () -> useCase.execute(roleId));
        verify(roleRepository, times(0)).delete(any());
    }

    @Test
    void mustThrowRoleInUseExceptionWhenRoleIsAssignedToAnApiClient() {
        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin"));
        when(roleRepository.isAssignedToAnyUser(roleId)).thenReturn(false);
        when(roleRepository.isAssignedToAnyApiClient(roleId)).thenReturn(true);

        assertThrows(RoleInUseException.class, () -> useCase.execute(roleId));
        verify(roleRepository, times(0)).delete(any());
    }
}
