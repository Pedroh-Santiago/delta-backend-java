package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.PersonalDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPersonalDocumentRepository extends JpaRepository<PersonalDocumentEntity, UUID> {
    List<PersonalDocumentEntity> findAllByCustomerId(UUID customerId);
    List<PersonalDocumentEntity> findAllByCustomerIdIn(Set<UUID> customerIds);
}
