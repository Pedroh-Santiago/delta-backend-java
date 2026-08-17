package br.com.deltaglobalbank.identity.features.roles.deleteRoles;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleInUseException;
import br.com.deltaglobalbank.identity.domain.role.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.role.RoleProtectedException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteRoleUseCase {

    private static final String PLATFORM_ADMIN_CODE = "platform.admin";

    private static final Logger log = LoggerFactory.getLogger(DeleteRoleUseCase.class);

    private final RoleRepository roleRepository;

    public DeleteRoleUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void execute(UUID roleId) {
        Role role = roleRepository.findById(roleId);
        if (role == null) {
            throw new RoleNotFoundException();
        }

        if (role.getCode().value().equals(PLATFORM_ADMIN_CODE)) {
            throw new RoleProtectedException();
        }

        if (roleRepository.isAssignedToAnyUser(roleId) || roleRepository.isAssignedToAnyApiClient(roleId)) {
            throw new RoleInUseException();
        }

        roleRepository.delete(roleId);
        log.info("role soft-deleted roleId={}", roleId);
    }
}
