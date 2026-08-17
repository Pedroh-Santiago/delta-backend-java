package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeyUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.RotateSigningKeyResponse;
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.RotateSigningKeyUseCase;
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyRotator;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RotateSigningKeyUseCaseTests {

    private final SigningKeyRotator rotation = mock(SigningKeyRotator.class);
    private final KeyManager keyManager = mock(KeyManager.class);

    private RotateSigningKeyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RotateSigningKeyUseCase(rotation, keyManager);
    }

    @Test
    void mustRotateKeyAndRefreshKeyManagerWithPreviousKey() {
        UUID rotatedBy = UUID.randomUUID();

        SigningKey newKey = SigningKey.create("key-new", "RS256", "pub", "priv");
        SigningKey previousKey = SigningKey.create("key-old", "RS256", "pub-old", "priv-old");
        when(rotation.rotate()).thenReturn(new SigningKeyRotator.RotationResult(newKey, previousKey));
        doNothing().when(keyManager).refresh();

        RotateSigningKeyResponse response = useCase.execute(rotatedBy);

        Assertions.assertAll(
            () -> assertNotNull(response.newKey()),
            () -> assertNotNull(response.previousKey()),
            () -> assertEquals("key-new", response.newKey().kid())
        );
        verify(rotation, times(1)).rotate();
        verify(keyManager, times(1)).refresh();
    }

    @Test
    void mustRotateKeyWithNullPreviousKeyOnFirstRotation() {
        UUID rotatedBy = UUID.randomUUID();

        SigningKey newKey = SigningKey.create("key-new", "RS256", "pub", "priv");
        when(rotation.rotate()).thenReturn(new SigningKeyRotator.RotationResult(newKey, null));
        doNothing().when(keyManager).refresh();

        RotateSigningKeyResponse response = useCase.execute(rotatedBy);

        Assertions.assertAll(
            () -> assertNotNull(response.newKey()),
            () -> assertEquals("key-new", response.newKey().kid()),
            () -> assertNull(response.previousKey())
        );
        verify(rotation, times(1)).rotate();
        verify(keyManager, times(1)).refresh();
    }
}
