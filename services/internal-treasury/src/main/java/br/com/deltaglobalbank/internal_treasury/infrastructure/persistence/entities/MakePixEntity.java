package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pix_payments")
public class MakePixEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private long accountId;

    @Column(name = "recipient_institution_code", nullable = false)
    private String recipientInstitutionCode;

    @Column(name = "recipient_branch_code", nullable = false)
    private String recipientBranchCode;

    @Column(name = "recipient_account_number", nullable = false)
    private String recipientAccountNumber;

    @Column(name = "recipient_account_type", nullable = false)
    private String recipientAccountType;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "operation_amount", nullable = false)
    private long operationAmount;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status")
    private String status;

    protected MakePixEntity() {
    }

    public MakePixEntity(
        UUID id,
        long accountId,
        String recipientInstitutionCode,
        String recipientBranchCode,
        String recipientAccountNumber,
        String recipientAccountType,
        String recipientName,
        long operationAmount,
        Instant paidAt,
        Instant createdAt,
        String status
    ) {
        this.id = id;
        this.accountId = accountId;
        this.recipientInstitutionCode = recipientInstitutionCode;
        this.recipientBranchCode = recipientBranchCode;
        this.recipientAccountNumber = recipientAccountNumber;
        this.recipientAccountType = recipientAccountType;
        this.recipientName = recipientName;
        this.operationAmount = operationAmount;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getRecipientInstitutionCode() {
        return recipientInstitutionCode;
    }

    public String getRecipientBranchCode() {
        return recipientBranchCode;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public String getRecipientAccountType() {
        return recipientAccountType;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public long getOperationAmount() {
        return operationAmount;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
}
