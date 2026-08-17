package br.com.deltaglobalbank.identity.features.users.updateUser;

import java.util.List;
import java.util.UUID;

public record UpdateUserCommand(
    UUID userId,
    String fullName,
    String email,
    UUID actorTenantId,
    List<String> actorRoles,
    UUID actorId
) {
}
