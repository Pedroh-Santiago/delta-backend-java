package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeyUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.CannotRevokeActiveKeyException;
import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyNotFoundException;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys.SigningKeyRevoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SigningKeyRevokerTests {

    private final SigningKeyRepository signingKeyRepository = mock(SigningKeyRepository.class);
    private SigningKeyRevoker revoker;

    @BeforeEach
    void setUp() {
        revoker = new SigningKeyRevoker(signingKeyRepository);
    }

    @Test
    void mustThrow404WhenKeyDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(signingKeyRepository.findById(id)).thenReturn(null);
        assertThrows(SigningKeyNotFoundException.class, () -> revoker.revoke(id));
        verify(signingKeyRepository, times(0)).save(any());
    }

    @Test
    void mustThrow409WhenKeyIsActive() {
        SigningKey active = SigningKey.create("key-active", "RS256", "pub", "priv");
        when(signingKeyRepository.findById(any())).thenReturn(active);
        assertThrows(CannotRevokeActiveKeyException.class, () -> revoker.revoke(active.getId()));
        verify(signingKeyRepository, times(0)).save(any());
    }

    @Test
    void mustRevokeARetiredKey() {
        SigningKey retired = SigningKey.create("key-ret", "RS256", "pub", "priv");
        retired.retire();
        when(signingKeyRepository.findById(any())).thenReturn(retired);
        when(signingKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean mutated = revoker.revoke(retired.getId());

        assertTrue(mutated);
        assertEquals(SigningKeyStatus.REVOKED, retired.status());
        verify(signingKeyRepository, times(1)).save(any());
    }

    @Test
    void mustBeIdempotentWhenKeyAlreadyRevoked() {
        SigningKey revoked = SigningKey.create("key-rev", "RS256", "pub", "priv");
        revoked.revoke();
        when(signingKeyRepository.findById(any())).thenReturn(revoked);
        assertFalse(revoker.revoke(revoked.getId()));
        verify(signingKeyRepository, times(0)).save(any());
    }
}
