package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findAllByTenantId(UUID tenantId);

    Page<UserEntity> findAllByTenantId(UUID tenantId, Pageable pageable);
}
