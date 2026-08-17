package br.com.deltaglobalbank.identity.features.users.listUserRoles;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUserRoleUseCase {

    private final UserRepository userRepository;
    private final JpaUserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;

    public ListUserRoleUseCase(
        UserRepository userRepository,
        JpaUserRoleRepository userRoleRepository,
        RoleRepository roleRepository,
        ModuleRepository moduleRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional(readOnly = true)
    public ListUserRolesResponse execute(ListUserRoleQuery query) {
        User user = userRepository.findById(query.userId());
        if (user == null) {
            throw new UserNotFound();
        }
        if (!user.getTenantId().equals(query.tenantId())) {
            throw new UserNotFound();
        }

        List<UserRoleEntity> entries = userRoleRepository.findAllByUserId(user.getId());
        if (entries.isEmpty()) {
            return new ListUserRolesResponse(List.of());
        }

        Set<UUID> roleIds = entries.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toSet());
        Map<UUID, Role> rolesById = roleRepository.findAllByIds(roleIds).stream()
            .collect(Collectors.toMap(Role::getId, r -> r));

        Set<UUID> moduleIds = rolesById.values().stream()
            .map(Role::getModuleId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        Map<UUID, String> moduleCodeById = moduleRepository.findAllByIds(moduleIds).stream()
            .collect(Collectors.toMap(Module::getId, m -> m.getCode().value()));

        List<ListedUserRoles> items = entries.stream()
            .map(entry -> {
                Role role = rolesById.get(entry.getRoleId());
                if (role == null) {
                    return null;
                }
                return new ListedUserRoles(
                    role.getCode().value(),
                    role.getId(),
                    role.getModuleId() != null ? moduleCodeById.get(role.getModuleId()) : null,
                    entry.getGrantedAt(),
                    entry.getGrantedBy()
                );
            })
            .filter(java.util.Objects::nonNull)
            .toList();

        return new ListUserRolesResponse(items);
    }
}
