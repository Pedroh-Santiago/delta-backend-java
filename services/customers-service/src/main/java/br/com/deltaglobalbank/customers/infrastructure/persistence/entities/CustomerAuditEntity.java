package br.com.deltaglobalbank.customers.infrastructure.persistence.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_audit")
public class CustomerAuditEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected CustomerAuditEntity() {
    }

    public CustomerAuditEntity(
        UUID id,
        UUID customerId,
        UUID tenantId,
        String action,
        String oldValue,
        String newValue,
        UUID changedBy,
        Instant changedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.tenantId = tenantId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getAction() {
        return action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
