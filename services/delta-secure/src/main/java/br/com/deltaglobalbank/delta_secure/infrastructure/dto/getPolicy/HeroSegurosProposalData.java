package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosProposalData(
    Integer id,
    @JsonProperty("partner_plan_id") Integer partnerPlanId,
    @JsonProperty("customer_id") Integer customerId,
    @JsonProperty("guarantor_id") Integer guarantorId,
    @JsonProperty("policy_purchase_info_id") Integer policyPurchaseInfoId,
    Integer status,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt,
    HeroSegurosProposalInfo info,
    List<HeroSegurosProposalCoverage> coverages,
    @JsonProperty("partner_plan") HeroSegurosProposalPlan partnerPlan,
    HeroSegurosProposalCustomer customer,
    Object guarantor
) {
    public HeroSegurosProposalData {
        if (coverages == null) {
            coverages = List.of();
        }
    }

    public HeroSegurosProposalData() {
        this(null, null, null, null, null, null, null, null, null, List.of(), null, null, null);
    }
}
