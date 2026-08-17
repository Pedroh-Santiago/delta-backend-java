package br.com.deltaglobalbank.delta_secure.domain.policy;

import java.time.Instant;
import java.util.UUID;

public record TermoAdesaoReference(
    UUID id,
    String ticket,
    int heroSegurosId,
    Convenio convenio,
    String externalId,
    Instant createdAt
) {
}
