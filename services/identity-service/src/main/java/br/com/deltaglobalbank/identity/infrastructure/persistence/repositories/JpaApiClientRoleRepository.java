package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ApiClientRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaApiClientRoleRepository extends JpaRepository<ApiClientRoleEntity, UUID> {
    List<ApiClientRoleEntity> findAllByApiClientId(UUID apiClientId);

    ApiClientRoleEntity findByApiClientIdAndRoleId(UUID apiClientId, UUID roleId);

    List<ApiClientRoleEntity> findAllByApiClientIdIn(Set<UUID> apiClientIds);
}
