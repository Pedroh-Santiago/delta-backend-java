package br.com.deltaglobalbank.identity.domain.token;

import java.util.List;
import java.util.UUID;

public interface SigningKeyRepository {
    SigningKey findById(UUID id);

    SigningKey findByKid(String kid);

    List<SigningKey> findAllByStatus(SigningKeyStatus status);

    SigningKey findFirstActive();

    SigningKey save(SigningKey signingKey);
}
