package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

import java.util.List;

public record HeroSegurosGetPolicyResponse(
    Boolean success,
    HeroSegurosProposalData data,
    List<String> notifications
) {
    public HeroSegurosGetPolicyResponse(HeroSegurosProposalData data) {
        this(null, data, null);
    }
}
