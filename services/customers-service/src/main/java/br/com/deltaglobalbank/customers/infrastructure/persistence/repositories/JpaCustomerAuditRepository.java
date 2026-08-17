package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories;

import java.util.UUID;

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerAuditRepository extends JpaRepository<CustomerAuditEntity, UUID> {
}
