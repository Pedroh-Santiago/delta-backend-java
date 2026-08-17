package br.com.deltaglobalbank.identity.features.users.removeUserRole;

import java.util.Set;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import br.com.deltaglobalbank.identity.domain.user.CannotRemoveOwnAdminRoleException;
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserRoleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveUserRoleUseCase {

    private static final Set<String> ADMIN_ROLES = Set.of("identity.admin", "platform.admin");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JpaUserRoleRepository userRoleRepository;

    public RemoveUserRoleUseCase(
        UserRepository userRepository,
        RoleRepository roleRepository,
        JpaUserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public void execute(RemoveUserRoleCommand command) {
        User user = userRepository.findById(command.userId());
        if (user == null) {
            throw new UserNotFound();
        }
        if (!user.getTenantId().equals(command.tenantId())) {
            throw new UserNotFound();
        }

        if (command.principal().subject().equals(command.userId())
            && ADMIN_ROLES.contains(command.roleCode().value())) {
            throw new CannotRemoveOwnAdminRoleException();
        }

        Role role = roleRepository.findByCode(command.roleCode());
        if (role == null) {
            throw new RoleNotFoundException(command.roleCode().value());
        }

        UserRoleEntity entry = userRoleRepository.findByUserIdAndRoleId(user.getId(), role.getId());
        if (entry == null) {
            return;
        }

        userRoleRepository.delete(entry);
    }
}
