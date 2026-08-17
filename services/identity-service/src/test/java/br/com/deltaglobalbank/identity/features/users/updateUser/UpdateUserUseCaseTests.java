package br.com.deltaglobalbank.identity.features.users.updateUser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.AccessTokenRevoker;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenRevoker;
import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateUserUseCaseTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AccessTokenRevoker accessTokenRevoker = mock(AccessTokenRevoker.class);
    private final RefreshTokenRevoker refreshTokenRevoker = mock(RefreshTokenRevoker.class);

    private UpdateUserUseCase useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserUseCase(userRepository, accessTokenRevoker, refreshTokenRevoker);
    }

    private User user(UUID tenant) {
        return new User(
            UUID.randomUUID(), tenant,
            "João Silva", new Email("joao@empresa.com"),
            new HashedPassword("hash"),
            UserStatus.ACTIVE,
            false, null, null,
            0, null,
            Instant.now(), Instant.now()
        );
    }

    private User user() {
        return user(tenantId);
    }

    private UpdateUserCommand command(UUID userId, String name, String email, List<String> roles) {
        return new UpdateUserCommand(userId, name, email, tenantId, roles, actor);
    }

    private UpdateUserCommand command(UUID userId, String name, String email) {
        return command(userId, name, email, List.of("identity.admin"));
    }

    @Test
    void changingEmailRevokesAccessAndRefreshTokens() {
        User u = user();
        when(userRepository.findById(u.getId())).thenReturn(u);
        when(userRepository.existsByEmail(new Email("novo@empresa.com"))).thenReturn(false);
        when(userRepository.save(u)).thenReturn(u);

        useCase.execute(command(u.getId(), "João Silva", "novo@empresa.com"));

        verify(userRepository).save(u);
        verify(accessTokenRevoker).revokeUser(u.getId());
        verify(refreshTokenRevoker).revokeAllForUser(u.getId());
    }

    @Test
    void changingOnlyTheNameDoesNotRevokeTokens() {
        User u = user();
        when(userRepository.findById(u.getId())).thenReturn(u);
        when(userRepository.save(u)).thenReturn(u);

        useCase.execute(command(u.getId(), "João da Silva Santos", "joao@empresa.com"));

        verify(userRepository).save(u);
        verify(accessTokenRevoker, never()).revokeUser(any());
        verify(refreshTokenRevoker, never()).revokeAllForUser(any());
    }

    @Test
    void duplicateEmailThrowsEmailAlreadyExists() {
        User u = user();
        when(userRepository.findById(u.getId())).thenReturn(u);
        when(userRepository.existsByEmail(new Email("tomado@empresa.com"))).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
            () -> useCase.execute(command(u.getId(), "João Silva", "tomado@empresa.com")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void keepingTheSameEmailDoesNotCheckUniquenessNorRevoke() {
        User u = user();
        when(userRepository.findById(u.getId())).thenReturn(u);
        when(userRepository.save(u)).thenReturn(u);

        useCase.execute(command(u.getId(), "João da Silva Santos", "joao@empresa.com"));

        verify(userRepository, never()).existsByEmail(any());
        verify(refreshTokenRevoker, never()).revokeAllForUser(any());
    }

    @Test
    void identityAdminEditingAnotherTenantGets404() {
        User u = user(UUID.randomUUID());
        when(userRepository.findById(u.getId())).thenReturn(u);

        assertThrows(UserNotFound.class, () -> useCase.execute(command(u.getId(), "X", "x@empresa.com")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void idempotentUpdateDoesNotPersist() {
        User u = user();
        when(userRepository.findById(u.getId())).thenReturn(u);

        useCase.execute(command(u.getId(), "João Silva", "joao@empresa.com"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void missingUserThrowsUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(null);

        assertThrows(UserNotFound.class, () -> useCase.execute(command(id, "X", "x@empresa.com")));
    }
}
