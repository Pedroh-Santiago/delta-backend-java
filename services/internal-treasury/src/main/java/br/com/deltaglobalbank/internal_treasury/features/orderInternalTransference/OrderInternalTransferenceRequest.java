package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference;

import jakarta.validation.constraints.NotNull;

public record OrderInternalTransferenceRequest(
    @NotNull(message = "CPF do Pagador é obrigatório")
    String payerCpf,
    @NotNull(message = "CPF do Recebedor é obrigatório")
    String receiverCpf,
    @NotNull(message = "Valor é obrigatório")
    Integer amount,
    String description
) {
}
