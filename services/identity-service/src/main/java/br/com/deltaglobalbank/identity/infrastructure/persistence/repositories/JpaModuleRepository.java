package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaModuleRepository extends JpaRepository<ModuleEntity, UUID> {
    ModuleEntity findByCode(String code);
}
