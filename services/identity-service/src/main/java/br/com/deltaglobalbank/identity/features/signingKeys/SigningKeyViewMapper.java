package br.com.deltaglobalbank.identity.features.signingKeys;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyView;

public final class SigningKeyViewMapper {

    private SigningKeyViewMapper() {
    }

    public static SigningKeyView toView(SigningKey signingKey) {
        return new SigningKeyView(
            signingKey.getId(),
            signingKey.getKid(),
            signingKey.getAlgorithm(),
            signingKey.status().toDatabaseValue(),
            signingKey.getActivatedAt(),
            signingKey.snapshot().retiredAt()
        );
    }
}
