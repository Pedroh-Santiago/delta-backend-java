package br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosPolicySummary;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesResponse;

public final class SearchPoliciesMappers {

    private SearchPoliciesMappers() {
    }

    public static HeroSegurosSearchPoliciesRequest toHeroSegurosSearchPoliciesRequest(SearchPoliciesRequest request) {
        return new HeroSegurosSearchPoliciesRequest(
            request.createdAt(),
            request.createdAtEnd(),
            request.operation()
        );
    }

    public static SearchPoliciesResponse toSearchPoliciesResponse(HeroSegurosSearchPoliciesResponse response) {
        return new SearchPoliciesResponse(
            response.data().recordsTotal(),
            response.data().recordsFiltered(),
            response.data().data().stream().map(SearchPoliciesMappers::toPolicySummary).toList()
        );
    }

    public static PolicySummary toPolicySummary(HeroSegurosPolicySummary summary) {
        return new PolicySummary(
            summary.id(),
            summary.ticket(),
            summary.plan().name(),
            summary.customer().name(),
            summary.customer().docNumber(),
            summary.info().price(),
            summary.info().debtAmount(),
            summary.info().installments(),
            summary.status(),
            summary.createdAt()
        );
    }
}
