package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

public record IssuePolicyRequest(
    Convenio convenio,
    boolean billed,
    @Positive double debtAmount,
    @Positive int installments,
    String externalId,
    String lastInstallmentDate,
    @Positive Double chargedAmount,
    @Valid IssuePolicyCliente customer,
    String identificadorProcessoSWorks,
    @Positive Double netAmount,
    @Positive Double installmentAmount
) {
}
