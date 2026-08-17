package br.com.deltaglobalbank.identity.features.users.updateUser;

import java.time.Instant;
import java.util.UUID;

public record UpdateUserResponse(
    UUID id,
    String fullName,
    String email,
    UUID tenantId,
    String status,
    Instant updatedAt
) {
}
