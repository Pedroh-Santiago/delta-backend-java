package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosPolicySummaryInfo(
    int id,
    @JsonProperty("price_cents") int priceCents,
    double price,
    double iof,
    @JsonProperty("debt_amount") double debtAmount,
    int installments,
    @JsonProperty("installments_amount") String installmentsAmount,
    @JsonProperty("type_of_charge_id") int typeOfChargeId,
    @JsonProperty("start_date") String startDate,
    @JsonProperty("end_date") String endDate,
    Integer days
) {
}
