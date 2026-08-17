package br.com.deltaglobalbank.identity.features.apiClients.createApiKey

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateApiKeyRequest(
    @field:NotBlank(message = "name é obrigatório")
    @field:Size(min = 3, max = 255, message = "name deve ter entre 3 e 255 caracteres")
    val name: String,

    val expiresAt: Instant? = null
)