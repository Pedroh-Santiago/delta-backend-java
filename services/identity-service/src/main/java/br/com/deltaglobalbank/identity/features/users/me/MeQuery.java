package br.com.deltaglobalbank.identity.features.users.me;

import java.util.UUID;

public record MeQuery(
    UUID principalId,
    String principalType
) {
}
