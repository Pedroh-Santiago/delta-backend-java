package br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosNoPlanAvailable;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.IssuePolicyMappers;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationPlan;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationResponse;

public final class QuotationMappers {

    private QuotationMappers() {
    }

    public static HeroSegurosQuotationRequest toHeroSegurosQuotationRequest(QuotationRequest request, int typeOfProduct) {
        return new HeroSegurosQuotationRequest(
            request.debtAmount(),
            request.installments(),
            typeOfProduct,
            new HeroSegurosQuotationCustomer(request.age())
        );
    }

    public static QuotationResponse toQuotationResponse(HeroSegurosQuotationResponse response) {
        HeroSegurosQuotationPlan plan = response.data().stream().findFirst()
            .orElseThrow(HeroSegurosNoPlanAvailable::new);
        return new QuotationResponse(
            plan.name(),
            plan.price(),
            plan.coverages().stream().map(IssuePolicyMappers::toCoverage).toList()
        );
    }
}
