package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record IssuePolicyCliente(
    @NotBlank String name,
    @NotBlank String docNumber,
    @NotBlank String birthday,
    int civil,
    @NotBlank String gender,
    @NotBlank String phone,
    @NotBlank @Email String email,
    @Valid IssuePolicyEndereco address
) {
}
