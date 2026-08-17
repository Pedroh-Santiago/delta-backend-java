package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeySnapshot;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity;

public final class SigningKeyMapper {

    private SigningKeyMapper() {
    }

    public static SigningKey toDomain(SigningKeyEntity entity) {
        return new SigningKey(
            entity.getId(),
            entity.getKid(),
            entity.getAlgorithm(),
            entity.getPublicKey(),
            entity.getPrivateKey(),
            SigningKeyStatus.fromDatabaseValue(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getActivatedAt(),
            entity.getRetiredAt()
        );
    }

    public static SigningKeyEntity toEntity(SigningKey signingKey) {
        SigningKeySnapshot s = signingKey.snapshot();
        return new SigningKeyEntity(
            s.id(),
            s.kid(),
            s.algorithm(),
            s.publicKey(),
            s.privateKey(),
            s.status().toDatabaseValue(),
            s.createdAt(),
            s.activatedAt(),
            s.retiredAt()
        );
    }

    public static SigningKeyEntity applyTo(SigningKey signingKey, SigningKeyEntity entity) {
        SigningKeySnapshot s = signingKey.snapshot();
        entity.setStatus(s.status().toDatabaseValue());
        entity.setRetiredAt(s.retiredAt());
        return entity;
    }
}
