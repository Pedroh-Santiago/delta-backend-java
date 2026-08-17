package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

import java.util.List;

public record HeroSegurosSearchPoliciesData(
    int draw,
    int recordsTotal,
    int recordsFiltered,
    List<HeroSegurosPolicySummary> data
) {
}
