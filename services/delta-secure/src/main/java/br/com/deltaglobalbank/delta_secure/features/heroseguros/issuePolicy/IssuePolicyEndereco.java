package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import jakarta.validation.constraints.NotBlank;

public record IssuePolicyEndereco(
    @NotBlank String cep,
    @NotBlank String address,
    @NotBlank String number,
    String complement,
    @NotBlank String neighborhood,
    @NotBlank String city,
    @NotBlank String state
) {
}
