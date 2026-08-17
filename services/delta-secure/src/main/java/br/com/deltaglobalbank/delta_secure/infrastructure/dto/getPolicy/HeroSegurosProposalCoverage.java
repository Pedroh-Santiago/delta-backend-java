package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosProposalCoverage(
    Integer id,
    @JsonProperty("coverage_name") String coverageName,
    @JsonProperty("is") String insuredAmount,
    @JsonProperty("is_limit") String insuredAmountLimit,
    @JsonProperty("installments_limit") Object installmentsLimit,
    @JsonProperty("installment_max_limit") String installmentMaxLimit,
    @JsonProperty("coverage_type_name") String coverageTypeName,
    @JsonProperty("debit_percent") Object debitPercent,
    @JsonProperty("deductible_days") Object deductibleDays,
    @JsonProperty("waiting_period_days") Integer waitingPeriodDays,
    @JsonProperty("is_info") Object isInfo,
    Object info
) {
    public HeroSegurosProposalCoverage() {
        this(null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
