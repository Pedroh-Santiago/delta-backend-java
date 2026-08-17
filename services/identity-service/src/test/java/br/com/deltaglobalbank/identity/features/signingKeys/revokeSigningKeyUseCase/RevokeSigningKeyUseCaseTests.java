package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeyUseCase;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys.RevokeSigningKeyUseCase;
import br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys.SigningKeyRevoker;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeSigningKeyUseCaseTests {

    private final SigningKeyRevoker revoker = mock(SigningKeyRevoker.class);
    private final KeyManager keyManager = mock(KeyManager.class);

    private RevokeSigningKeyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RevokeSigningKeyUseCase(revoker, keyManager);
    }

    @Test
    void mustRefreshKeyManagerWhenRevocationMutatedAKey() {
        UUID keyId = UUID.randomUUID();
        UUID revokedBy = UUID.randomUUID();

        when(revoker.revoke(keyId)).thenReturn(true);
        doNothing().when(keyManager).refresh();

        useCase.execute(keyId, revokedBy);

        verify(keyManager, times(1)).refresh();
    }

    @Test
    void mustNotRefreshKeyManagerWhenRevocationDidNotMutate() {
        UUID keyId = UUID.randomUUID();
        UUID revokedBy = UUID.randomUUID();

        when(revoker.revoke(keyId)).thenReturn(false);

        useCase.execute(keyId, revokedBy);

        verify(keyManager, times(0)).refresh();
    }
}
