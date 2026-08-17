package br.com.deltaglobalbank.identity.domain.module;

import java.time.Instant;
import java.util.UUID;

public record ModuleSnapshot(
    UUID id,
    ModuleCode code,
    String name,
    String description,
    Instant createdAt
) {
}
