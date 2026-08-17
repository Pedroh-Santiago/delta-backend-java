package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity;

public final class CustomerAuditMapper {

    private CustomerAuditMapper() {
    }

    public static CustomerAuditEntity toEntity(CustomerAuditEntry entry) {
        return new CustomerAuditEntity(
            entry.id(),
            entry.customerId(),
            entry.tenantId(),
            entry.action().toDatabaseValue(),
            entry.oldValue(),
            entry.newValue(),
            entry.changedBy(),
            entry.changedAt()
        );
    }
}
