package br.com.deltaglobalbank.identity.features.apiClients.createApiKey;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiKeyRequest(
    @NotBlank(message = "name é obrigatório")
    @Size(min = 3, max = 255, message = "name deve ter entre 3 e 255 caracteres")
    String name,

    Instant expiresAt
) {
}
