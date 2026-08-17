package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "internal_transferences")
public class InternalTransferenceEntity {

    @Id
    private UUID id;

    @Column(name = "requested_by_id", nullable = false)
    private UUID requestedById;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "payer_id", nullable = false)
    private long payerId;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    @Column(name = "account_number", nullable = false)
    private long accountNumber;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    protected InternalTransferenceEntity() {
    }

    public InternalTransferenceEntity(
        UUID id,
        UUID requestedById,
        Instant requestedAt,
        long payerId,
        Instant paidAt,
        long accountNumber,
        int amount,
        String description,
        String status
    ) {
        this.id = id;
        this.requestedById = requestedById;
        this.requestedAt = requestedAt;
        this.payerId = payerId;
        this.paidAt = paidAt;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.description = description;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequestedById() {
        return requestedById;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public long getPayerId() {
        return payerId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}
