package br.com.deltaglobalbank.delta_secure.infrastructure.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosTokenRequest(
    @JsonProperty("grant_type") String grantType,
    @JsonProperty("client_id") String clientId,
    @JsonProperty("client_secret") String clientSecret,
    String username,
    String password,
    String scope
) {
}
