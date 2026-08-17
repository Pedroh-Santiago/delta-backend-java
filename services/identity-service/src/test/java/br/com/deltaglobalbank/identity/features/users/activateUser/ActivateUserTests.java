package br.com.deltaglobalbank.identity.features.users.activateUser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActivateUserTests {

    private final UserRepository userRepository = mock(UserRepository.class);

    private ActivateUserUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new ActivateUserUseCase(userRepository);
    }

    @Test
    void activatesUserWhenInSameTenant() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(tenantId);
        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        useCase.activateUser(userId, tenantId, UUID.randomUUID());

        verify(user, times(1)).activate();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void throwsWhenUserNotFound() {
        when(userRepository.findById(any())).thenReturn(null);
        assertThrows(UserNotFound.class,
            () -> useCase.activateUser(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void throwsWhenUserBelongsToAnotherTenant() {
        User user = mock(User.class);
        when(user.getTenantId()).thenReturn(UUID.randomUUID());
        when(userRepository.findById(any())).thenReturn(user);
        assertThrows(UserNotFound.class,
            () -> useCase.activateUser(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
    }
}
