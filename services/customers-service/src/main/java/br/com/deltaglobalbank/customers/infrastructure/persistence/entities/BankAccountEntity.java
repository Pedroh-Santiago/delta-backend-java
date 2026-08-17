package br.com.deltaglobalbank.customers.infrastructure.persistence.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "customer_bank_accounts")
@SQLDelete(sql = "UPDATE customers.customer_bank_accounts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class BankAccountEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "agency", nullable = false)
    private String agency;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_digit")
    private String accountDigit;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected BankAccountEntity() {
    }

    public BankAccountEntity(
        UUID id,
        UUID customerId,
        String bankCode,
        String agency,
        String accountNumber,
        String accountDigit,
        String accountType,
        String purpose,
        boolean isPrimary,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.bankCode = bankCode;
        this.agency = agency;
        this.accountNumber = accountNumber;
        this.accountDigit = accountDigit;
        this.accountType = accountType;
        this.purpose = purpose;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getAgency() {
        return agency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountDigit() {
        return accountDigit;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getPurpose() {
        return purpose;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
