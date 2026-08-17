package br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCustomer;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroSegurosPolicyRequest(
    boolean billed,
    @JsonProperty("partner_plan_id") int partnerPlanId,
    @JsonProperty("type_of_product") int typeOfProduct,
    @JsonProperty("external_id") String externalId,
    @JsonProperty("debt_amount") double debtAmount,
    @JsonProperty("charged_amount") Double chargedAmount,
    int installments,
    @JsonProperty("last_installment_date") String lastInstallmentDate,
    HeroSegurosCustomer customer
) {
}
