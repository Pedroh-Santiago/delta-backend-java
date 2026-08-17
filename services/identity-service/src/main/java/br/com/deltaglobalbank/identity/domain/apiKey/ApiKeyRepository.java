package br.com.deltaglobalbank.identity.domain.apiKey;

import java.util.UUID;

public interface ApiKeyRepository {
    ApiKey save(ApiKey apiKey);

    ApiKey findById(UUID id);

    ApiKey findByFingerprint(String fingerprint);
}
