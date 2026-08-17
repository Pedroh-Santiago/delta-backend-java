package br.com.deltaglobalbank.identity.features.users.createUser;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.RoleCode;

public record CreatedUser(
    UUID id,
    String fullName,
    String email,
    UUID tenantId,
    List<RoleCode> roles,
    boolean mustChangePassword,
    Instant createdAt
) {
}
