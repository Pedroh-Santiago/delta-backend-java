package br.com.deltaglobalbank.delta_secure.features.heroseguros.cancelPolicy;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.cancelPolicy.HeroSegurosCancelPolicyRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.cancelPolicy.HeroSegurosCancelPolicyResponse;

public final class CancelPolicyMappers {

    private CancelPolicyMappers() {
    }

    public static HeroSegurosCancelPolicyRequest toHeroSegurosCancelPolicyRequest(CancelPolicyRequest request) {
        return new HeroSegurosCancelPolicyRequest(
            request.docNumber(),
            request.ticket(),
            request.reason()
        );
    }

    public static CancelPolicyResponse toCancelPolicyResponse(HeroSegurosCancelPolicyResponse response) {
        return new CancelPolicyResponse(
            response.success(),
            response.notifications()
        );
    }
}
