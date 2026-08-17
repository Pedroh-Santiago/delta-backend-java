package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.util.List;
import java.util.UUID;

public record UserClaims(
    UUID userId,
    UUID tenantId,
    List<String> roles,
    List<String> modules,
    boolean mustChangePassword
) {
}
