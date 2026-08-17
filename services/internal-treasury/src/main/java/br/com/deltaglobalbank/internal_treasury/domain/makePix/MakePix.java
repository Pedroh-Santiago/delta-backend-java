package br.com.deltaglobalbank.internal_treasury.domain.makePix;

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import java.time.Instant;
import java.util.UUID;

public class MakePix {

    private final UUID id;
    private final long accountId;
    private final String recipientInstitutionCode;
    private final String recipientBranchCode;
    private final String recipientAccountNumber;
    private final String recipientAccountType;
    private final String recipientName;
    private final long operationAmount;
    private final Instant createdAt;

    private PaymentsStatus status;
    private Instant paidAt = null;

    public MakePix(
        UUID id,
        long accountId,
        String recipientInstitutionCode,
        String recipientBranchCode,
        String recipientAccountNumber,
        PaymentsStatus status,
        Instant paidAt,
        String recipientAccountType,
        String recipientName,
        long operationAmount,
        Instant createdAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.recipientInstitutionCode = recipientInstitutionCode;
        this.recipientBranchCode = recipientBranchCode;
        this.recipientAccountNumber = recipientAccountNumber;
        this.status = status;
        this.recipientAccountType = recipientAccountType;
        this.recipientName = recipientName;
        this.operationAmount = operationAmount;
        this.createdAt = createdAt;

        if (accountId <= 0) {
            throw new IllegalArgumentException("Account Id precisa ser positivo");
        }
        if (recipientInstitutionCode.isBlank()) {
            throw new IllegalArgumentException("Código da instituição é obrigatório");
        }
        if (recipientBranchCode.isBlank()) {
            throw new IllegalArgumentException("Agência da conta é obrigatória");
        }
        if (recipientAccountNumber.isBlank()) {
            throw new IllegalArgumentException("Número da conta do recebedor é obrigatória");
        }
        if (recipientAccountType.isBlank()) {
            throw new IllegalArgumentException("Tipo da conta do recebedor é obrigatório");
        }
        if (recipientName.isBlank()) {
            throw new IllegalArgumentException("Nome do recebedor é obrigatório");
        }
        if (operationAmount <= 0) {
            throw new IllegalArgumentException("Valor da operação precisa ser positivo");
        }
    }

    public MakePix(
        UUID id,
        long accountId,
        String recipientInstitutionCode,
        String recipientBranchCode,
        String recipientAccountNumber,
        PaymentsStatus status,
        String recipientAccountType,
        String recipientName,
        long operationAmount,
        Instant createdAt
    ) {
        this(id, accountId, recipientInstitutionCode, recipientBranchCode, recipientAccountNumber,
            status, null, recipientAccountType, recipientName, operationAmount, createdAt);
    }

    public MakePix(
        UUID id,
        long accountId,
        String recipientInstitutionCode,
        String recipientBranchCode,
        String recipientAccountNumber,
        String recipientAccountType,
        String recipientName,
        long operationAmount,
        Instant createdAt
    ) {
        this(id, accountId, recipientInstitutionCode, recipientBranchCode, recipientAccountNumber,
            PaymentsStatus.WAITING, null, recipientAccountType, recipientName, operationAmount, createdAt);
    }

    public void approve() {
        if (status != PaymentsStatus.WAITING) {
            throw new IllegalArgumentException("Apenas PIX com status de Waiting podem ser alterados");
        }
        status = PaymentsStatus.APPROVED;
    }

    public void markAsPaid(Instant paidAt) {
        if (status != PaymentsStatus.APPROVED) {
            throw new IllegalArgumentException("Apenas PIX que foram aprovados podem ser marcados como pago");
        }
        status = PaymentsStatus.PAID;
        this.paidAt = paidAt;
    }

    public UUID id() {
        return id;
    }

    public long accountId() {
        return accountId;
    }

    public String recipientInstitutionCode() {
        return recipientInstitutionCode;
    }

    public String recipientBranchCode() {
        return recipientBranchCode;
    }

    public String recipientAccountNumber() {
        return recipientAccountNumber;
    }

    public String recipientAccountType() {
        return recipientAccountType;
    }

    public String recipientName() {
        return recipientName;
    }

    public long operationAmount() {
        return operationAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public PaymentsStatus status() {
        return status;
    }

    public Instant paidAt() {
        return paidAt;
    }
}
