package br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies;

public record HeroSegurosSearchPoliciesResponse(
    boolean success,
    HeroSegurosSearchPoliciesData data,
    String notifications
) {
    public HeroSegurosSearchPoliciesResponse(boolean success, HeroSegurosSearchPoliciesData data) {
        this(success, data, null);
    }
}
