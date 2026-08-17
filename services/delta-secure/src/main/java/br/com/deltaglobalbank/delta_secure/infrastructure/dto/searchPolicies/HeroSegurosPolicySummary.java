package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosPolicySummary(
    Integer id,
    String ticket,
    HeroSegurosPolicySummaryPlan plan,
    HeroSegurosPolicySummaryCustomer customer,
    HeroSegurosPolicySummaryInfo info,
    int status,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt
) {
    public HeroSegurosPolicySummary(
        String ticket,
        HeroSegurosPolicySummaryPlan plan,
        HeroSegurosPolicySummaryCustomer customer,
        HeroSegurosPolicySummaryInfo info,
        int status,
        String createdAt,
        String updatedAt
    ) {
        this(null, ticket, plan, customer, info, status, createdAt, updatedAt);
    }
}
