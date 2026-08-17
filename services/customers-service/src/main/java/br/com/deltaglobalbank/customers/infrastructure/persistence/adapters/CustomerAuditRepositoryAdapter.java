package br.com.deltaglobalbank.customers.infrastructure.persistence.adapters;

import java.util.List;

import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditEntry;
import br.com.deltaglobalbank.customers.domain.audit.CustomerAuditRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.CustomerAuditMapper;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerAuditRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerAuditRepositoryAdapter implements CustomerAuditRepository {

    private final JpaCustomerAuditRepository jpa;

    public CustomerAuditRepositoryAdapter(JpaCustomerAuditRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void saveAll(List<CustomerAuditEntry> entries) {
        List<CustomerAuditEntity> entities = entries.stream()
            .map(CustomerAuditMapper::toEntity)
            .toList();
        jpa.saveAll(entities);
    }
}
