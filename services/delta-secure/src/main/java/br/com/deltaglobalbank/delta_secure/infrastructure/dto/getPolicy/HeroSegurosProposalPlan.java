package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosProposalPlan(
    @JsonProperty("partner_plan_id") Integer partnerPlanId,
    String name,
    @JsonProperty("has_policy_period") Boolean hasPolicyPeriod,
    @JsonProperty("policy_period_months") Integer policyPeriodMonths,
    @JsonProperty("loan_term") Object loanTerm,
    @JsonProperty("loan_term_months") Object loanTermMonths
) {
    public HeroSegurosProposalPlan() {
        this(null, null, null, null, null, null);
    }
}
