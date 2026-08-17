package br.com.deltaglobalbank.identity.features.users.me;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeResponse(
    UUID id,
    String principalType,
    String fullName,
    UUID tenantId,
    String tenantSlug,
    String tenantName,
    String status,
    List<String> roles,
    List<String> modules,
    Instant createdAt,
    String email,
    Boolean mustChangePassword,
    Instant lastLoginAt,
    String name,
    String description
) {
}
