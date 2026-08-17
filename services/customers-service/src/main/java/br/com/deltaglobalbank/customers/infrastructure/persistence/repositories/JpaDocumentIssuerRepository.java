package br.com.deltaglobalbank.customers.infrastructure.persistence.repositories;

import java.util.UUID;

import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.DocumentIssuerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDocumentIssuerRepository extends JpaRepository<DocumentIssuerEntity, UUID> {
    DocumentIssuerEntity findByName(String name);
}
