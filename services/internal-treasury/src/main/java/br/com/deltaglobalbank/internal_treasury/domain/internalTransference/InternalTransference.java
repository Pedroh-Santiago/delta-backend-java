package br.com.deltaglobalbank.internal_treasury.domain.internalTransference;

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import java.time.Instant;
import java.util.UUID;

public class InternalTransference {

    private final UUID id;
    private final long accountNumber;
    private final long payerId;
    private final int amount;
    private final String description;
    private final Instant requestedAt;
    private final UUID requestedById;

    private PaymentsStatus status;
    private Instant paidAt = null;

    public InternalTransference(
        UUID id,
        long accountNumber,
        long payerId,
        Instant paidAt,
        int amount,
        PaymentsStatus status,
        String description,
        Instant requestedAt,
        UUID requestedById
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.payerId = payerId;
        this.amount = amount;
        this.status = status;
        this.description = description;
        this.requestedAt = requestedAt;
        this.requestedById = requestedById;
    }

    public InternalTransference(
        UUID id,
        long accountNumber,
        long payerId,
        int amount,
        PaymentsStatus status,
        String description,
        Instant requestedAt,
        UUID requestedById
    ) {
        this(id, accountNumber, payerId, null, amount, status, description, requestedAt, requestedById);
    }

    public InternalTransference(
        UUID id,
        long accountNumber,
        long payerId,
        int amount,
        String description,
        Instant requestedAt,
        UUID requestedById
    ) {
        this(id, accountNumber, payerId, null, amount, PaymentsStatus.WAITING, description, requestedAt, requestedById);
    }

    public void approve() {
        if (status != PaymentsStatus.WAITING) {
            throw new IllegalArgumentException("Apenas TEF'S com status de Waiting podem ser alterados");
        }
        status = PaymentsStatus.APPROVED;
    }

    public void markAsPaid(Instant paidAt) {
        if (status != PaymentsStatus.APPROVED) {
            throw new IllegalArgumentException("Podemos marcar como pago apenas os que já foram aprovados");
        }
        status = PaymentsStatus.PAID;
        this.paidAt = paidAt;
    }

    public UUID id() {
        return id;
    }

    public long accountNumber() {
        return accountNumber;
    }

    public long payerId() {
        return payerId;
    }

    public int amount() {
        return amount;
    }

    public String description() {
        return description;
    }

    public Instant requestedAt() {
        return requestedAt;
    }

    public UUID requestedById() {
        return requestedById;
    }

    public PaymentsStatus status() {
        return status;
    }

    public Instant paidAt() {
        return paidAt;
    }
}
