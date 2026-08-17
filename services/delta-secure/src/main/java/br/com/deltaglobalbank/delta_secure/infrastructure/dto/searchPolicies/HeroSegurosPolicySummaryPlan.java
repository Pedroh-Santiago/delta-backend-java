package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosPolicySummaryPlan(
    @JsonProperty("partner_plan_id") int partnerPlanId,
    String name,
    @JsonProperty("has_policy_period") boolean hasPolicyPeriod,
    @JsonProperty("policy_period_months") Integer policyPeriodMonths,
    @JsonProperty("loan_term") Integer loanTerm,
    @JsonProperty("loan_term_months") Integer loanTermMonths
) {
}
