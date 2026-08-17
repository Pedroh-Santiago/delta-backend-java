package br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import jakarta.validation.constraints.Positive;

public record QuotationRequest(
    Convenio convenio,
    @Positive double debtAmount,
    @Positive int installments,
    @Positive int age
) {
}
