package br.com.deltaglobalbank.identity.domain.role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleRepository {
    Role findById(UUID id);

    Role findByCode(RoleCode code);

    List<Role> findAll();

    List<Role> findAllByIds(Set<UUID> ids);

    List<Role> findAllByModuleId(UUID moduleId);

    List<Role> findAllByUserId(UUID userId);

    List<Role> findAllByApiClientId(UUID apiClientId);
}
