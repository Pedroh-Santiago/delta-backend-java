package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.UUID;

public interface ApiKeyActiveCount {
    UUID getApiClientId();

    Long getTotal();
}
