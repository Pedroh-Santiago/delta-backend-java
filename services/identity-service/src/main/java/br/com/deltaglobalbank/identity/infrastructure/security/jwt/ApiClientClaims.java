package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.util.List;
import java.util.UUID;

public record ApiClientClaims(
    UUID apiClientId,
    UUID tenantId,
    List<String> roles,
    List<String> modules
) {
}
