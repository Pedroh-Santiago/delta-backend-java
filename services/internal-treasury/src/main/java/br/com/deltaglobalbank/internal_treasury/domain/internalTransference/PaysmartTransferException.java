package br.com.deltaglobalbank.internal_treasury.domain.internalTransference;

public class PaysmartTransferException extends RuntimeException {
    public PaysmartTransferException(long payerAccountNumber) {
        super("Falha ao processar transferência da conta " + payerAccountNumber);
    }
}
