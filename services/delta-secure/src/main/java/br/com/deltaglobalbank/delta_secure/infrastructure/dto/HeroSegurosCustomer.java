package br.com.deltaglobalbank.delta_secure.infrastructure.dto;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosAddress;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosCustomer(
    String name,
    @JsonProperty("doc_number") String docNumber,
    String birthday,
    int civil,
    String gender,
    String phone,
    String email,
    HeroSegurosAddress address
) {
}
