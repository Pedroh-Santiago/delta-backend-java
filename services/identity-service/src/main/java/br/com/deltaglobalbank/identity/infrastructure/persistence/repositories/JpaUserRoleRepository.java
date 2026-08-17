package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {
    List<UserRoleEntity> findAllByUserId(UUID userId);

    UserRoleEntity findByUserIdAndRoleId(UUID userId, UUID roleId);

    List<UserRoleEntity> findAllByUserIdIn(Set<UUID> userIds);

    boolean existsByRoleIdAndDeletedAtIsNull(UUID roleId);

    List<UserRoleEntity> findAllByRoleId(UUID roleId);
}
