package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosSearchPoliciesRequest(
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("created_at_end") String createdAtEnd,
    String operation
) {
}
