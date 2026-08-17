package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosPolicySummaryCustomer(
    int id,
    String name,
    @JsonProperty("doc_number") String docNumber,
    String email
) {
}
