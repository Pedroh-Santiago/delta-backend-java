package br.com.deltaglobalbank.identity.features.assignUserRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException;
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AssignRoleUseCase {

    private final TenantModuleRepository tenantModuleRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JpaUserRoleRepository jpaUserRoleRepository;

    public AssignRoleUseCase(
        TenantModuleRepository tenantModuleRepository,
        ModuleRepository moduleRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        JpaUserRoleRepository jpaUserRoleRepository
    ) {
        this.tenantModuleRepository = tenantModuleRepository;
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jpaUserRoleRepository = jpaUserRoleRepository;
    }

    private void validateRoleBelowPlatformAdmin(List<Role> roles, List<String> creatorRoles) {
        if (creatorRoles.contains("platform.admin")) {
            return;
        }
        for (Role role : roles) {
            if (role.getCode().value().equals("platform.admin")) {
                throw new AuthorizationDeniedException("denied");
            }
        }
    }

    private void validateRolesAgainstTenantModules(List<Role> roles, UUID tenantId) {
        List<Role> rolesNeedingModule = roles.stream().filter(it -> it.getModuleId() != null).toList();
        if (rolesNeedingModule.isEmpty()) {
            return;
        }

        Set<UUID> enabledModuleIds = tenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, true).stream()
            .map(it -> it.getModuleId())
            .collect(Collectors.toSet());

        for (Role role : rolesNeedingModule) {
            if (!enabledModuleIds.contains(role.getModuleId())) {
                Module module = moduleRepository.findById(role.getModuleId());
                throw new ModuleNotEnabledForTenantException(module != null ? module.getCode().value() : "unknown");
            }
        }
    }

    @Transactional
    public AssignRolesResponse assignRoleTenant(
        UUID actingTenantId,
        AuthenticatedPrincipal principal,
        UUID userId,
        AssignRolesRequest request
    ) {
        User validUser = userRepository.findById(userId);
        if (validUser == null) {
            throw new UserNotFound();
        }

        if (!validUser.getTenantId().equals(actingTenantId)) {
            throw new UserNotFound();
        }

        List<Role> requestedRoles = request.rolesCodes().stream()
            .map(code -> {
                Role role = roleRepository.findByCode(code);
                if (role == null) {
                    throw new RoleNotFoundException(code.toString());
                }
                return role;
            })
            .toList();

        validateRoleBelowPlatformAdmin(requestedRoles, principal.roles());
        validateRolesAgainstTenantModules(requestedRoles, actingTenantId);

        List<Role> currentRoles = roleRepository.findAllByUserId(validUser.getId());
        Set<Role> currentRoleSet = Set.copyOf(currentRoles);

        List<Role> rolesToAdd = requestedRoles.stream().filter(it -> !currentRoleSet.contains(it)).toList();

        for (Role role : rolesToAdd) {
            jpaUserRoleRepository.save(new UserRoleEntity(
                UuidCreator.getTimeOrderedEpoch(),
                validUser.getId(),
                role.getId(),
                Instant.now(),
                principal.subject(),
                null
            ));
        }

        List<RoleCode> addedRoles = rolesToAdd.stream().map(Role::getCode).toList();
        List<RoleCode> userCodeRoles = new ArrayList<>();
        currentRoles.forEach(it -> userCodeRoles.add(it.getCode()));
        rolesToAdd.forEach(it -> userCodeRoles.add(it.getCode()));

        return new AssignRolesResponse(validUser.getId(), addedRoles, userCodeRoles);
    }
}
