package br.com.deltaglobalbank.delta_secure.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosCoverage(
    int id,
    @JsonProperty("coverage_name") String coverageName,
    @JsonProperty("is") String insuredAmount,
    @JsonProperty("waiting_period_days") Integer waitingPeriodDays
) {
}
