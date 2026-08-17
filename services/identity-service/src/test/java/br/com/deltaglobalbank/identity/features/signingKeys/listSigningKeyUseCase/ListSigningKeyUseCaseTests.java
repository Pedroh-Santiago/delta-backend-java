package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeyUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys.ListSigningKeysResponse;
import br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys.ListSigningKeysUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListSigningKeyUseCaseTests {

    private final SigningKeyRepository signingKeyRepository = mock(SigningKeyRepository.class);

    private ListSigningKeysUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSigningKeysUseCase(signingKeyRepository);
    }

    @Test
    void mustReturnEmptyListWhenThereAreNoSigningKeys() {
        when(signingKeyRepository.findAllByStatus(any())).thenReturn(List.of());

        ListSigningKeysResponse response = useCase.execute();

        assertTrue(response.items().isEmpty());
    }

    @Test
    void mustListSigningKeysFromAllStatuses() {
        SigningKey activeKey = SigningKey.create("key-active", "RS256", "pub", "priv");
        SigningKey retiredKey = SigningKey.create("key-retired", "RS256", "pub2", "priv2");
        retiredKey.retire();
        SigningKey revokedKey = SigningKey.create("key-revoked", "RS256", "pub3", "priv3");
        revokedKey.revoke();

        when(signingKeyRepository.findAllByStatus(SigningKeyStatus.ACTIVE)).thenReturn(List.of(activeKey));
        when(signingKeyRepository.findAllByStatus(SigningKeyStatus.RETIRED)).thenReturn(List.of(retiredKey));
        when(signingKeyRepository.findAllByStatus(SigningKeyStatus.REVOKED)).thenReturn(List.of(revokedKey));

        ListSigningKeysResponse response = useCase.execute();

        Assertions.assertAll(
            () -> assertEquals(3, response.items().size()),
            () -> assertTrue(response.items().stream().anyMatch(it -> it.kid().equals("key-active"))),
            () -> assertTrue(response.items().stream().anyMatch(it -> it.kid().equals("key-retired"))),
            () -> assertTrue(response.items().stream().anyMatch(it -> it.kid().equals("key-revoked")))
        );
    }
}
