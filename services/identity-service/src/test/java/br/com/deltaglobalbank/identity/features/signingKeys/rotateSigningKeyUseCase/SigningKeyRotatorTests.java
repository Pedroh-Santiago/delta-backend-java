package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeyUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.KeyPair;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyRotator;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SigningKeyRotatorTests {

    private final SigningKeyRepository signingKeyRepository = mock(SigningKeyRepository.class);
    private final KeyGenerator keyGenerator = mock(KeyGenerator.class);

    private SigningKeyRotator rotator;

    @BeforeEach
    void setUp() {
        rotator = new SigningKeyRotator(signingKeyRepository, keyGenerator);

        KeyPair fakePair = mock(KeyPair.class);
        when(keyGenerator.generateRsaKeyPair()).thenReturn(fakePair);
        when(keyGenerator.encodePublicKey(any())).thenReturn("pub-new");
        when(keyGenerator.encodePrivateKey(any())).thenReturn("priv-new");

        when(signingKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void mustRetireThePreviousKeyAndCreateANewActiveKey() {
        SigningKey previous = SigningKey.create("key-old", "RS256", "pub-old", "priv-old");
        when(signingKeyRepository.findFirstActive()).thenReturn(previous);

        SigningKeyRotator.RotationResult result = rotator.rotate();
        SigningKey newKey = result.newKey();
        SigningKey prev = result.previousKey();

        Assertions.assertAll(
            () -> assertEquals(SigningKeyStatus.RETIRED, prev.status()),
            () -> assertEquals(SigningKeyStatus.ACTIVE, newKey.status()),
            () -> assertNotEquals(prev.getKid(), newKey.getKid())
        );

        verify(signingKeyRepository, times(2)).save(any());
    }

    @Test
    void mustCreateANewActiveKeyWithoutRetiringWhenThereIsNoPreviousKey() {
        when(signingKeyRepository.findFirstActive()).thenReturn(null);

        SigningKeyRotator.RotationResult result = rotator.rotate();

        Assertions.assertAll(
            () -> assertNull(result.previousKey()),
            () -> assertEquals(SigningKeyStatus.ACTIVE, result.newKey().status())
        );

        verify(signingKeyRepository, times(1)).save(any());
    }
}
