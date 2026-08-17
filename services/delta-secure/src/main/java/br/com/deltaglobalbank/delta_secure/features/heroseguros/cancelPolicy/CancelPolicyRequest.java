package br.com.deltaglobalbank.delta_secure.features.heroseguros.cancelPolicy;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CancelPolicyRequest(
    Convenio convenio,
    @NotBlank String docNumber,
    @NotBlank String ticket,
    @Positive int reason
) {
}
