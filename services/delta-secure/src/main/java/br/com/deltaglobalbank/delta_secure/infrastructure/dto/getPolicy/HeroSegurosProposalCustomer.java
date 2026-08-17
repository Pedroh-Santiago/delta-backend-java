package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosProposalCustomer(
    Integer id,
    @JsonProperty("customer_address_id") Integer customerAddressId,
    @JsonProperty("customer_civil_id") Integer customerCivilId,
    @JsonProperty("customer_gender_id") Integer customerGenderId,
    String name,
    @JsonProperty("doc_type") String docType,
    @JsonProperty("doc_number") String docNumber,
    String birthday,
    String phone,
    String cellphone,
    String email,
    HeroSegurosProposalGender gender,
    HeroSegurosProposalCivil civil,
    HeroSegurosProposalAddress address
) {
    public HeroSegurosProposalCustomer() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
