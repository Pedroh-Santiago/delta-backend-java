package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosProposalInfo(
    Integer id,
    @JsonProperty("price_cents") Integer priceCents,
    Double price,
    Double iof,
    @JsonProperty("debt_amount") Double debtAmount,
    Integer installments,
    @JsonProperty("installments_amount") String installmentsAmount,
    @JsonProperty("type_of_charge_id") Integer typeOfChargeId,
    @JsonProperty("start_date") String startDate,
    @JsonProperty("end_date") String endDate,
    Integer days
) {
    public HeroSegurosProposalInfo() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }
}
