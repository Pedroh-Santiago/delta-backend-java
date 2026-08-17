package br.com.deltaglobalbank.internal_treasury.domain.makePix;

public class PaysmartPixException extends RuntimeException {
    public PaysmartPixException(long accountId) {
        super("Falha ao realizar o pix da conta " + accountId);
    }
}
