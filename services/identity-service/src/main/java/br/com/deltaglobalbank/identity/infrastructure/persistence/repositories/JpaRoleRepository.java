package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findByCode(String code);

    List<RoleEntity> findAllByModuleId(UUID moduleId);

    @Query("""
    SELECT r FROM RoleEntity r
    JOIN ApiClientRoleEntity acr ON acr.roleId = r.id
    WHERE acr.apiClientId = :apiClientId
      AND acr.deletedAt IS NULL
""")
    List<RoleEntity> findAllByApiClientId(@Param("apiClientId") UUID apiClientId);
}
