package br.com.deltaglobalbank.identity.features.roles.updateRoleStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleProtectedException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiClientRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaApiKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.exchangeApiKey.ExchangeApiKeyCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateRoleStatusUseCaseTests {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final JpaUserRoleRepository jpaUserRoleRepository = mock(JpaUserRoleRepository.class);
    private final JpaApiClientRoleRepository jpaApiClientRoleRepository = mock(JpaApiClientRoleRepository.class);
    private final JpaApiKeyRepository jpaApiKeyRepository = mock(JpaApiKeyRepository.class);
    private final AccessTokenRevoker accessTokenRevoker = mock(AccessTokenRevoker.class);
    private final RefreshTokenRevoker refreshTokenRevoker = mock(RefreshTokenRevoker.class);
    private final ExchangeApiKeyCache exchangeApiKeyCache = mock(ExchangeApiKeyCache.class);

    private UpdateRoleStatusUseCase useCase;

    private final UUID roleId = UUID.randomUUID();

    private Role role(String code, boolean active) {
        return new Role(roleId, new RoleCode(code), null, null, null, active, Instant.now());
    }

    @BeforeEach
    void setUp() {
        useCase = new UpdateRoleStatusUseCase(
            roleRepository, jpaUserRoleRepository, jpaApiClientRoleRepository,
            jpaApiKeyRepository, accessTokenRevoker, refreshTokenRevoker, exchangeApiKeyCache
        );
    }

    @Test
    void mustThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        when(roleRepository.findById(roleId)).thenReturn(null);

        assertThrows(RoleNotFoundException.class,
            () -> useCase.execute(new UpdateRoleStatusCommand(roleId, false)));
    }

    @Test
    void mustThrowRoleProtectedExceptionWhenTryingToDeactivatePlatformAdmin() {
        when(roleRepository.findById(roleId)).thenReturn(role("platform.admin", true));

        assertThrows(RoleProtectedException.class,
            () -> useCase.execute(new UpdateRoleStatusCommand(roleId, false)));
        verify(roleRepository, times(0)).save(any());
    }

    @Test
    void mustActivatePlatformAdminWithoutRestriction() {
        when(roleRepository.findById(roleId)).thenReturn(role("platform.admin", true));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateRoleStatusResponse response = useCase.execute(new UpdateRoleStatusCommand(roleId, true));

        assertEquals(true, response.active());
    }

    @Test
    void mustDeactivateRoleWithoutUsersOrApiClientsAndNotRevokeAnything() {
        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin", true));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jpaUserRoleRepository.findAllByRoleId(roleId)).thenReturn(List.of());
        when(jpaApiClientRoleRepository.findAllByRoleId(roleId)).thenReturn(List.of());

        UpdateRoleStatusResponse response = useCase.execute(new UpdateRoleStatusCommand(roleId, false));

        assertEquals(false, response.active());
        verify(accessTokenRevoker, times(0)).revokeUser(any());
        verify(refreshTokenRevoker, times(0)).revokeAllForUser(any());
        verify(jpaApiKeyRepository, times(0)).findAllByApiClientIdIn(any());
    }

    @Test
    void mustRevokeSessionsOfAffectedUsersWhenDeactivating() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin", true));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jpaUserRoleRepository.findAllByRoleId(roleId)).thenReturn(List.of(
            new UserRoleEntity(UUID.randomUUID(), userId1, roleId, Instant.now(), null, null),
            new UserRoleEntity(UUID.randomUUID(), userId2, roleId, Instant.now(), null, null)
        ));
        when(jpaApiClientRoleRepository.findAllByRoleId(roleId)).thenReturn(List.of());
        doNothing().when(accessTokenRevoker).revokeUser(any());
        doNothing().when(refreshTokenRevoker).revokeAllForUser(any());

        useCase.execute(new UpdateRoleStatusCommand(roleId, false));

        verify(accessTokenRevoker, times(1)).revokeUser(userId1);
        verify(accessTokenRevoker, times(1)).revokeUser(userId2);
        verify(refreshTokenRevoker, times(1)).revokeAllForUser(userId1);
        verify(refreshTokenRevoker, times(1)).revokeAllForUser(userId2);
    }

    @Test
    void mustRevokeSessionsAndInvalidateCacheOfAffectedApiClientsWhenDeactivating() {
        UUID apiClientId = UUID.randomUUID();
        String fingerprint = "fp-123";

        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin", true));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jpaUserRoleRepository.findAllByRoleId(roleId)).thenReturn(List.of());
        when(jpaApiClientRoleRepository.findAllByRoleId(roleId)).thenReturn(List.of(
            new ApiClientRoleEntity(UUID.randomUUID(), apiClientId, roleId, Instant.now(), null)
        ));
        doNothing().when(accessTokenRevoker).revokeUser(apiClientId);
        when(jpaApiKeyRepository.findAllByApiClientIdIn(Set.of(apiClientId))).thenReturn(List.of(
            new ApiKeyEntity(
                UUID.randomUUID(), apiClientId, "key", "hash", "prefix", fingerprint,
                null, null, null, Instant.now(), Instant.now(), null
            )
        ));
        doNothing().when(exchangeApiKeyCache).invalidate(fingerprint);

        useCase.execute(new UpdateRoleStatusCommand(roleId, false));

        verify(accessTokenRevoker, times(1)).revokeUser(apiClientId);
        verify(exchangeApiKeyCache, times(1)).invalidate(fingerprint);
    }

    @Test
    void mustNotRevokeAnythingWhenReactivatingARole() {
        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin", false));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateRoleStatusResponse response = useCase.execute(new UpdateRoleStatusCommand(roleId, true));

        assertEquals(true, response.active());
        verify(jpaUserRoleRepository, times(0)).findAllByRoleId(any());
        verify(jpaApiClientRoleRepository, times(0)).findAllByRoleId(any());
    }

    @Test
    void mustNotRevokeAgainWhenDeactivatingAnAlreadyInactiveRole() {
        when(roleRepository.findById(roleId)).thenReturn(role("internal-payroll.admin", false));
        when(roleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new UpdateRoleStatusCommand(roleId, false));

        verify(jpaUserRoleRepository, times(0)).findAllByRoleId(any());
        verify(jpaApiClientRoleRepository, times(0)).findAllByRoleId(any());
    }
}
