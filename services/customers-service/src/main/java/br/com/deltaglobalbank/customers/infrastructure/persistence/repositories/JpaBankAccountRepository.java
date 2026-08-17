package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBankAccountRepository extends JpaRepository<BankAccountEntity, UUID> {
    List<BankAccountEntity> findAllByCustomerId(UUID customerId);
    List<BankAccountEntity> findAllByCustomerIdIn(Set<UUID> customerIds);
}
