package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity

fun CustomerAuditEntry.toEntity() = CustomerAuditEntity(
    id = id,
    customerId = customerId,
    tenantId = tenantId,
    action = action.toDatabaseValue(),
    oldValue = oldValue,
    newValue = newValue,
    changedBy = changedBy,
    changedAt = changedAt
)