package br.com.deltaglobalbank.identity.features.jwks;

import java.util.List;

public record JwkResponse(
    List<Jwk> keys
) {
}
