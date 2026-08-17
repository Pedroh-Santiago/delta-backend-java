package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SWorksLoginResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") long expiresIn
) {
}
