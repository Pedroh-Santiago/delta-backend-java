package br.com.deltaglobalbank.customers.domain.audit

import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import java.util.UUID

data class CustomerAuditEntry(
    val id: UUID,
    val customerId: UUID,
    val tenantId: UUID,
    val action: CustomerAuditAction,
    val oldValue: String?,
    val newValue: String?,
    val changedBy: UUID,
    val changedAt: Instant
){
    companion object{
        fun bankAccountAdded(customerId: UUID, tenantId: UUID, newValue: String, changedBy: UUID) =
            CustomerAuditEntry(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customerId,
                tenantId = tenantId,
                action = CustomerAuditAction.BANK_ACCOUNT_ADDED,
                oldValue = null,
                newValue = newValue,
                changedBy = changedBy,
                changedAt = Instant.now(),
            )

        fun bankAccountRemoved(customerId: UUID, tenantId: UUID, oldValue: String, changedBy: UUID) =
            CustomerAuditEntry(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customerId,
                tenantId = tenantId,
                action = CustomerAuditAction.BANK_ACCOUNT_REMOVED,
                oldValue = oldValue,
                newValue = null,
                changedBy = changedBy,
                changedAt = Instant.now(),
            )

        fun cpfChanged(customerId: UUID, tenantId: UUID, oldValue: String, newValue: String, changedBy: UUID) =
            CustomerAuditEntry(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customerId,
                tenantId = tenantId,
                action = CustomerAuditAction.CPF_CHANGED,
                oldValue = oldValue,
                newValue = newValue,
                changedBy = changedBy,
                changedAt = Instant.now(),
            )

        fun customerDeleted(customerId: UUID, tenantId: UUID, changedBy: UUID) =
            CustomerAuditEntry(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customerId,
                tenantId = tenantId,
                action = CustomerAuditAction.CUSTOMER_DELETED,
                oldValue = null,
                newValue = null,
                changedBy = changedBy,
                changedAt = Instant.now(),
            )

        fun fullNameChanged(customerId: UUID, tenantId: UUID, oldValue: String, newValue: String, changedBy: UUID) =
            CustomerAuditEntry(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customerId,
                tenantId = tenantId,
                action = CustomerAuditAction.FULL_NAME_CHANGED,
                oldValue = oldValue,
                newValue = newValue,
                changedBy = changedBy,
                changedAt = Instant.now(),
            )

        fun statusChanged(customerId: UUID, tenantId: UUID, oldValue: String, newValue: String, changedBy: UUID) =
            CustomerAuditEntry(
                id = UuidCreator.getTimeOrderedEpoch(),
                customerId = customerId,
                tenantId = tenantId,
                action = CustomerAuditAction.STATUS_CHANGED,
                oldValue = oldValue,
                newValue = newValue,
                changedBy = changedBy,
                changedAt = Instant.now(),
            )
    }
}