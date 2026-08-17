package br.com.deltaglobalbank.identity.features.signingKeys.revokeSigningKeys;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RevokeSigningKeyUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevokeSigningKeyUseCase.class);

    private final SigningKeyRevoker revoker;
    private final KeyManager keyManager;

    public RevokeSigningKeyUseCase(SigningKeyRevoker revoker, KeyManager keyManager) {
        this.revoker = revoker;
        this.keyManager = keyManager;
    }

    public void execute(UUID id, UUID revokedBy) {
        boolean mutated = revoker.revoke(id);
        if (mutated) {
            keyManager.refresh();
            log.info("signing_key revoked keyId={} revokedBy={} at={}", id, revokedBy, Instant.now());
        }
    }
}
