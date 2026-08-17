package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.identity.features.signingKeys.SigningKeyViewMapper;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RotateSigningKeyUseCase {

    private static final Logger log = LoggerFactory.getLogger(RotateSigningKeyUseCase.class);

    private final SigningKeyRotator rotation;
    private final KeyManager keyManager;

    public RotateSigningKeyUseCase(SigningKeyRotator rotation, KeyManager keyManager) {
        this.rotation = rotation;
        this.keyManager = keyManager;
    }

    public RotateSigningKeyResponse execute(UUID rotatedBy) {
        SigningKeyRotator.RotationResult result = rotation.rotate();

        keyManager.refresh();

        log.info("signing_key rotated newKid={} previousKid={} rotatedBy={} at={}",
            result.newKey().getKid(), result.previousKey() != null ? result.previousKey().getKid() : null,
            rotatedBy, Instant.now());

        return new RotateSigningKeyResponse(
            SigningKeyViewMapper.toView(result.newKey()),
            result.previousKey() != null ? SigningKeyViewMapper.toView(result.previousKey()) : null
        );
    }
}
