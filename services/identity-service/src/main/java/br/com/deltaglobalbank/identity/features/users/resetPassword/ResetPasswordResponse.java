package br.com.deltaglobalbank.identity.features.users.resetPassword;

import java.time.Instant;
import java.util.UUID;

public record ResetPasswordResponse(
    UUID userId,
    String email,
    String temporaryPassword,
    boolean mustChangePassword,
    Instant resetAt
) {
}
