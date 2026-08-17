package br.com.deltaglobalbank.delta_secure.infrastructure.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosTokenResponse(
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") long expiresIn,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
) {
}
