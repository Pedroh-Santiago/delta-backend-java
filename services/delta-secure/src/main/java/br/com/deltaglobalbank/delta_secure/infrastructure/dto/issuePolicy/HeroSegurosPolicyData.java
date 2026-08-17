package br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy;

import java.util.List;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCoverage;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosPolicyData(
    @JsonProperty("partner_plan_id") int partnerPlanId,
    String name,
    @JsonProperty("has_policy_period") boolean hasPolicyPeriod,
    @JsonProperty("policy_period_months") int policyPeriodMonths,
    @JsonProperty("min_debt") String minDebt,
    @JsonProperty("max_debt") String maxDebt,
    @JsonProperty("min_age") int minAge,
    @JsonProperty("max_age") int maxAge,
    int installments,
    @JsonProperty("start_date") String startDate,
    @JsonProperty("end_date") String endDate,
    double iof,
    @JsonProperty("price_cents") int priceCents,
    String price,
    @JsonProperty("debt_amount") String debtAmount,
    List<HeroSegurosCoverage> coverages,
    HeroSegurosPolicy policy,
    Integer id
) {
    public HeroSegurosPolicyData(
        int partnerPlanId,
        String name,
        boolean hasPolicyPeriod,
        int policyPeriodMonths,
        String minDebt,
        String maxDebt,
        int minAge,
        int maxAge,
        int installments,
        String startDate,
        String endDate,
        double iof,
        int priceCents,
        String price,
        String debtAmount,
        List<HeroSegurosCoverage> coverages,
        HeroSegurosPolicy policy
    ) {
        this(
            partnerPlanId, name, hasPolicyPeriod, policyPeriodMonths, minDebt, maxDebt, minAge, maxAge,
            installments, startDate, endDate, iof, priceCents, price, debtAmount, coverages, policy, null
        );
    }
}
