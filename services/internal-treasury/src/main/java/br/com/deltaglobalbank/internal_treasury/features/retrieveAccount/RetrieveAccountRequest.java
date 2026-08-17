package br.com.deltaglobalbank.internal_treasury.features.retrieveAccount;

import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf;
import jakarta.validation.constraints.NotNull;

public record RetrieveAccountRequest(
    @NotNull(message = "CPF é obrigatório")
    Cpf cpf
) {
}
