package br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys;

import java.security.KeyPair;
import java.time.Instant;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.KeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SigningKeyRotator {

    private final SigningKeyRepository signingKeyRepository;
    private final KeyGenerator keyGenerator;

    public SigningKeyRotator(SigningKeyRepository signingKeyRepository, KeyGenerator keyGenerator) {
        this.signingKeyRepository = signingKeyRepository;
        this.keyGenerator = keyGenerator;
    }

    public record RotationResult(SigningKey newKey, SigningKey previousKey) {
    }

    @Transactional
    public RotationResult rotate() {
        SigningKey previous = signingKeyRepository.findFirstActive();
        if (previous != null) {
            previous.retire();
            signingKeyRepository.save(previous);
        }
        KeyPair pair = keyGenerator.generateRsaKeyPair();
        String kid = "key-" + Instant.now().toEpochMilli();
        SigningKey newKey = SigningKey.create(
            kid,
            "RS256",
            keyGenerator.encodePublicKey(pair.getPublic()),
            keyGenerator.encodePrivateKey(pair.getPrivate())
        );
        signingKeyRepository.save(newKey);
        return new RotationResult(newKey, previous);
    }
}
