package br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosQuotationRequest(
    @JsonProperty("debt_amount") double debtAmount,
    int installments,
    @JsonProperty("type_of_product") int typeOfProduct,
    HeroSegurosQuotationCustomer customer
) {
}
