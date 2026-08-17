package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.CannotRevokeActiveKeyException;
import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyNotFoundException;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SigningKeyRevoker {

    private final SigningKeyRepository signingKeyRepository;

    public SigningKeyRevoker(SigningKeyRepository signingKeyRepository) {
        this.signingKeyRepository = signingKeyRepository;
    }

    @Transactional
    public boolean revoke(UUID id) {
        SigningKey key = signingKeyRepository.findById(id);
        if (key == null) {
            throw new SigningKeyNotFoundException();
        }

        return switch (key.status()) {
            case ACTIVE -> throw new CannotRevokeActiveKeyException();
            case REVOKED -> false;
            case RETIRED -> {
                key.revoke();
                signingKeyRepository.save(key);
                yield true;
            }
        };
    }
}
