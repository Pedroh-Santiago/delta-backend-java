package br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation;

import java.util.List;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCoverage;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosQuotationPlan(
    @JsonProperty("partner_plan_id") int partnerPlanId,
    @JsonProperty("product_name") String productName,
    String name,
    @JsonProperty("min_debt") String minDebt,
    @JsonProperty("max_debt") String maxDebt,
    @JsonProperty("min_age") int minAge,
    @JsonProperty("max_age") int maxAge,
    int installments,
    String price,
    List<HeroSegurosCoverage> coverages
) {
}
