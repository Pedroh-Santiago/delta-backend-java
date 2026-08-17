package br.com.deltaglobalbank.delta_secure.infrastructure.dto.cancelPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosCancelPolicyRequest(
    @JsonProperty("doc_number") String docNumber,
    String ticket,
    int reason
) {
}
