package br.com.deltaglobalbank.identity.features.tokens.revokeToken;

import java.util.UUID;

public record RevokeJtiRequest(UUID jti) {
}
