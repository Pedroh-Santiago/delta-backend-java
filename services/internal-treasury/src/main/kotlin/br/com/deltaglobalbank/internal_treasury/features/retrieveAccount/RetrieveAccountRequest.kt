package br.com.deltaglobalbank.internal_treasury.features.retrieveAccount

import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf
import jakarta.validation.constraints.NotNull

data class RetrieveAccountRequest (
    @field:NotNull(message = "CPF é obrigatório")
    val cpf: Cpf
)