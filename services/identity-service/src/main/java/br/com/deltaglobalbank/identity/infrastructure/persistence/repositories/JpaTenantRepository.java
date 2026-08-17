package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTenantRepository extends JpaRepository<TenantEntity, UUID> {
    TenantEntity findBySlug(String slug);

    boolean existsBySlug(String slug);
}
