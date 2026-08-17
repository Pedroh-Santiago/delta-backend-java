package br.com.deltaglobalbank.customers.domain.audit;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public record CustomerAuditEntry(
    UUID id,
    UUID customerId,
    UUID tenantId,
    CustomerAuditAction action,
    String oldValue,
    String newValue,
    UUID changedBy,
    Instant changedAt
) {
    public CustomerAuditEntry {
        Objects.requireNonNull(id, "customer_audit_id_required");
        Objects.requireNonNull(customerId, "customer_audit_customer_id_required");
        Objects.requireNonNull(tenantId, "customer_audit_tenant_id_required");
        Objects.requireNonNull(action, "customer_audit_action_required");
        Objects.requireNonNull(changedBy, "customer_audit_changed_by_required");
        Objects.requireNonNull(changedAt, "customer_audit_changed_at_required");
    }
    public static CustomerAuditEntry bankAccountAdded(UUID customerId, UUID tenantId, String newValue, UUID changedBy) {
        return new CustomerAuditEntry(
            UuidCreator.getTimeOrderedEpoch(),
            customerId,
            tenantId,
            CustomerAuditAction.BANK_ACCOUNT_ADDED,
            null,
            newValue,
            changedBy,
            Instant.now()
        );
    }

    public static CustomerAuditEntry bankAccountRemoved(UUID customerId, UUID tenantId, String oldValue, UUID changedBy) {
        return new CustomerAuditEntry(
            UuidCreator.getTimeOrderedEpoch(),
            customerId,
            tenantId,
            CustomerAuditAction.BANK_ACCOUNT_REMOVED,
            oldValue,
            null,
            changedBy,
            Instant.now()
        );
    }

    public static CustomerAuditEntry cpfChanged(UUID customerId, UUID tenantId, String oldValue, String newValue, UUID changedBy) {
        return new CustomerAuditEntry(
            UuidCreator.getTimeOrderedEpoch(),
            customerId,
            tenantId,
            CustomerAuditAction.CPF_CHANGED,
            oldValue,
            newValue,
            changedBy,
            Instant.now()
        );
    }

    public static CustomerAuditEntry customerDeleted(UUID customerId, UUID tenantId, UUID changedBy) {
        return new CustomerAuditEntry(
            UuidCreator.getTimeOrderedEpoch(),
            customerId,
            tenantId,
            CustomerAuditAction.CUSTOMER_DELETED,
            null,
            null,
            changedBy,
            Instant.now()
        );
    }

    public static CustomerAuditEntry fullNameChanged(UUID customerId, UUID tenantId, String oldValue, String newValue, UUID changedBy) {
        return new CustomerAuditEntry(
            UuidCreator.getTimeOrderedEpoch(),
            customerId,
            tenantId,
            CustomerAuditAction.FULL_NAME_CHANGED,
            oldValue,
            newValue,
            changedBy,
            Instant.now()
        );
    }

    public static CustomerAuditEntry statusChanged(UUID customerId, UUID tenantId, String oldValue, String newValue, UUID changedBy) {
        return new CustomerAuditEntry(
            UuidCreator.getTimeOrderedEpoch(),
            customerId,
            tenantId,
            CustomerAuditAction.STATUS_CHANGED,
            oldValue,
            newValue,
            changedBy,
            Instant.now()
        );
    }
}
