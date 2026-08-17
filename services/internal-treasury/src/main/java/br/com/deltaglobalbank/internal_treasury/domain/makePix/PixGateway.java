package br.com.deltaglobalbank.internal_treasury.domain.makePix;

public interface PixGateway {
    long transferPix(
        long accountId,
        String recipientInstitutionCode,
        String recipientBranchCode,
        String recipientAccountNumber,
        String recipientAccountType,
        String recipientName,
        long operationAmount
    );
}
