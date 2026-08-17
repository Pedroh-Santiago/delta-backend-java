package br.com.deltaglobalbank.identity.features.users.suspendUser;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SuspendUserTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenRevoker refreshTokenRevoker = mock(RefreshTokenRevoker.class);
    private final AccessTokenRevoker accessTokenRevoker = mock(AccessTokenRevoker.class);

    private SuspendUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SuspendUserUseCase(refreshTokenRevoker, userRepository, accessTokenRevoker);
    }

    @Test
    void suspendsUserAndRevokesAccessAndRefreshTokens() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(accessTokenRevoker).revokeUser(userId);
        doNothing().when(refreshTokenRevoker).revokeAllForUser(userId);

        useCase.suspendUser(userId, tenantId, UUID.randomUUID());

        verify(user, times(1)).suspend();
        verify(userRepository, times(1)).save(user);
        verify(accessTokenRevoker, times(1)).revokeUser(userId);
        verify(refreshTokenRevoker, times(1)).revokeAllForUser(userId);
    }
}
