package br.com.deltaglobalbank.identity.features.users.resetPassword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.CannotResetOwnPasswordException;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.domain.user.UserSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.security.password.SpringPasswordHasher;
import br.com.deltaglobalbank.identity.infrastructure.security.password.TemporaryPasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class ResetPasswordUseCaseTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TemporaryPasswordGenerator temporaryPasswordGenerator = mock(TemporaryPasswordGenerator.class);
    private final SpringPasswordHasher passwordHasher = mock(SpringPasswordHasher.class);
    private final AccessTokenRevoker accessTokenRevoker = mock(AccessTokenRevoker.class);
    private final RefreshTokenRevoker refreshTokenRevoker = mock(RefreshTokenRevoker.class);

    private ResetPasswordUseCase useCase;
    private UUID userId;
    private UUID tenantId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(
            userRepository, temporaryPasswordGenerator, passwordHasher, accessTokenRevoker, refreshTokenRevoker
        );
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        adminId = UUID.randomUUID();
    }

    @Test
    void mustThrowCannotResetOwnPasswordWhenAdminResetsOwnPassword() {
        assertThrows(CannotResetOwnPasswordException.class, () -> useCase.execute(userId, tenantId, userId));

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(accessTokenRevoker, never()).revokeUser(any());
        verify(refreshTokenRevoker, never()).revokeAllForUser(any());
    }

    @Test
    void mustThrowUserNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(null);

        assertThrows(UserNotFound.class, () -> useCase.execute(userId, tenantId, adminId));

        verify(userRepository, never()).save(any());
        verify(accessTokenRevoker, never()).revokeUser(any());
        verify(refreshTokenRevoker, never()).revokeAllForUser(any());
    }

    @Test
    void mustThrowUserNotFoundWhenUserBelongsToAnotherTenant() {
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findById(userId)).thenReturn(user);

        assertThrows(UserNotFound.class, () -> useCase.execute(userId, tenantId, adminId));

        verify(userRepository, never()).save(any());
        verify(accessTokenRevoker, never()).revokeUser(any());
        verify(refreshTokenRevoker, never()).revokeAllForUser(any());
    }

    @Test
    void mustResetPasswordSaveAndRevokeTokensOnSuccess() {
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.getEmail()).thenReturn(new Email("user@example.com"));
        when(user.snapshot()).thenReturn(mock(UserSnapshot.class, RETURNS_DEFAULTS));
        when(userRepository.findById(userId)).thenReturn(user);

        when(temporaryPasswordGenerator.generatePassword()).thenReturn("TempPass123!");
        when(passwordHasher.hash(any())).thenReturn(new HashedPassword("hashed-temp"));
        when(userRepository.save(user)).thenReturn(user);

        ResetPasswordResponse response = useCase.execute(userId, tenantId, adminId);

        verify(user, times(1)).resetPassword(new HashedPassword("hashed-temp"));
        verify(userRepository, times(1)).save(user);
        verify(accessTokenRevoker, times(1)).revokeUser(userId);
        verify(refreshTokenRevoker, times(1)).revokeAllForUser(userId);

        assertEquals("TempPass123!", response.temporaryPassword());
        assertEquals(userId, response.userId());
        assertEquals("user@example.com", response.email());
        assertEquals(true, response.mustChangePassword());
    }

    @Test
    void mustRevokeTokensOnlyAfterSaving() {
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(user.getEmail()).thenReturn(new Email("user@example.com"));
        when(user.snapshot()).thenReturn(mock(UserSnapshot.class, RETURNS_DEFAULTS));
        when(userRepository.findById(userId)).thenReturn(user);
        when(temporaryPasswordGenerator.generatePassword()).thenReturn("TempPass123!");
        when(passwordHasher.hash(any())).thenReturn(new HashedPassword("hashed-temp"));
        when(userRepository.save(user)).thenReturn(user);

        useCase.execute(userId, tenantId, adminId);

        InOrder order = inOrder(userRepository, accessTokenRevoker, refreshTokenRevoker);
        order.verify(userRepository).save(user);
        order.verify(accessTokenRevoker).revokeUser(userId);
        order.verify(refreshTokenRevoker).revokeAllForUser(userId);
    }
}
