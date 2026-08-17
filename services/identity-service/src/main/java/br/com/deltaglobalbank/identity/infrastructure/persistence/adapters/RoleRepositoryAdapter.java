package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.RoleMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaRoleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRoleRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;

    public RoleRepositoryAdapter(JpaRoleRepository jpaRoleRepository, JpaUserRoleRepository jpaUserRoleRepository) {
        this.jpaRoleRepository = jpaRoleRepository;
        this.jpaUserRoleRepository = jpaUserRoleRepository;
    }

    @Override
    public Role findById(UUID id) {
        return jpaRoleRepository.findById(id).map(RoleMapper::toDomain).orElse(null);
    }

    @Override
    public Role findByCode(RoleCode code) {
        RoleEntity entity = jpaRoleRepository.findByCode(code.value());
        return entity != null ? RoleMapper.toDomain(entity) : null;
    }

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findAll().stream().map(RoleMapper::toDomain).toList();
    }

    @Override
    public List<Role> findAllByIds(Set<UUID> ids) {
        return jpaRoleRepository.findAllById(ids).stream().map(RoleMapper::toDomain).toList();
    }

    @Override
    public List<Role> findAllByModuleId(UUID moduleId) {
        return jpaRoleRepository.findAllByModuleId(moduleId).stream().map(RoleMapper::toDomain).toList();
    }

    @Override
    public List<Role> findAllByUserId(UUID userId) {
        Set<UUID> roleIds = jpaUserRoleRepository.findAllByUserId(userId).stream()
            .map(it -> it.getRoleId())
            .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return jpaRoleRepository.findAllById(roleIds).stream().map(RoleMapper::toDomain).toList();
    }

    @Override
    public List<Role> findAllByApiClientId(UUID apiClientId) {
        return jpaRoleRepository.findAllByApiClientId(apiClientId).stream().map(RoleMapper::toDomain).toList();
    }
}
