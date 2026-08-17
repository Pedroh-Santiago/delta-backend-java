package br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import jakarta.validation.constraints.NotBlank;

public record SearchPoliciesRequest(
    Convenio convenio,
    @NotBlank String createdAt,
    @NotBlank String createdAtEnd,
    String operation
) {
}
