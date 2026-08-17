package br.com.deltaglobalbank.identity.domain.role;

import java.time.Instant;
import java.util.UUID;

public record RoleSnapshot(
    UUID id,
    RoleCode code,
    UUID moduleId,
    String description,
    Instant createdAt
) {
}
