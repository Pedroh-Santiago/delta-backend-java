package br.com.deltaglobalbank.identity.features.users.listUsers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.domain.user.UserSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsersUseCase {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final JpaUserRoleRepository userRoleRepository;

    public ListUsersUseCase(
        UserRepository userRepository,
        TenantRepository tenantRepository,
        RoleRepository roleRepository,
        JpaUserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public ListUsersResponse execute(ListUsersQuery query) {
        if (query.requireTenantExists()) {
            UUID tenantId = query.tenantIdFilter();
            if (tenantId == null || tenantRepository.findById(tenantId) == null) {
                throw new TenantNotFoundException();
            }
        }

        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> usersPage = query.tenantIdFilter() != null
            ? userRepository.findPageByTenantId(query.tenantIdFilter(), pageable)
            : userRepository.findPage(pageable);

        if (usersPage.getContent().isEmpty()) {
            return new ListUsersResponse(List.of(), safePage, safeSize, 0, 0);
        }

        Set<UUID> tenantIds = usersPage.getContent().stream().map(User::getTenantId).collect(Collectors.toSet());
        Map<UUID, Tenant> tenantsById = tenantIds.stream()
            .map(tenantRepository::findById)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toMap(Tenant::getId, t -> t));

        Set<UUID> userIds = usersPage.getContent().stream().map(User::getId).collect(Collectors.toSet());
        Map<UUID, List<UserRoleEntity>> userRolesByUserId = userRoleRepository.findAllByUserIdIn(userIds).stream()
            .collect(Collectors.groupingBy(UserRoleEntity::getUserId));

        Set<UUID> roleIds = userRolesByUserId.values().stream()
            .flatMap(List::stream)
            .map(UserRoleEntity::getRoleId)
            .collect(Collectors.toSet());
        Map<UUID, Role> rolesById = roleIds.isEmpty()
            ? Map.of()
            : roleRepository.findAllByIds(roleIds).stream().collect(Collectors.toMap(Role::getId, r -> r));

        List<ListedUser> items = usersPage.getContent().stream()
            .map(user -> {
                Tenant tenant = tenantsById.get(user.getTenantId());
                List<String> roleCodes = userRolesByUserId.getOrDefault(user.getId(), List.of()).stream()
                    .map(it -> rolesById.get(it.getRoleId()))
                    .filter(java.util.Objects::nonNull)
                    .map(r -> r.getCode().value())
                    .toList();
                UserSnapshot snapshot = user.snapshot();
                return new ListedUser(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail().value(),
                    user.getTenantId(),
                    tenant != null ? tenant.getSlug() : "unknown",
                    snapshot.status().toDatabaseValue(),
                    roleCodes,
                    snapshot.mustChangePassword(),
                    snapshot.failedAttempts(),
                    snapshot.lockedUntil(),
                    snapshot.lastLoginAt(),
                    snapshot.createdAt()
                );
            })
            .toList();

        return new ListUsersResponse(
            items, safePage, safeSize, usersPage.getTotalElements(), usersPage.getTotalPages());
    }
}
