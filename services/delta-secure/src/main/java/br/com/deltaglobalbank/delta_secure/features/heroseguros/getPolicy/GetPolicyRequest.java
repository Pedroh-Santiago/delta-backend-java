package br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import jakarta.validation.constraints.Positive;

public record GetPolicyRequest(
    Convenio convenio,
    @Positive int id
) {
}
