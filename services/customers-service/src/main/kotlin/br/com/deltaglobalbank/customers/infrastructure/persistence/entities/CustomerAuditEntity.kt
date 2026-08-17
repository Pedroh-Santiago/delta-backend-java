package br.com.deltaglobalbank.customers.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import java.time.Instant

@Entity
@Table(name = "customer_audit")
class CustomerAuditEntity (
    @Id
    var id: UUID,

    @Column(name = "customer_id", nullable = false)
    var customerId: UUID,

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "action", nullable = false)
    var action: String,

    @Column(name = "old_value")
    var oldValue: String?,

    @Column(name = "new_value")
    var newValue: String?,

    @Column(name = "changed_by", nullable = false)
    var changedBy: UUID,

    @Column(name = "changed_at", nullable = false)
    var changedAt: Instant,
)