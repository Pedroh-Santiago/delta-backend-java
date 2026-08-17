package br.com.deltaglobalbank.identity.features.roles.createRoles;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleCodeAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRoleUseCase {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;

    public CreateRoleUseCase(RoleRepository roleRepository, ModuleRepository moduleRepository) {
        this.roleRepository = roleRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional
    public CreateRoleResponse execute(CreateRoleCommand command) {
        RoleCode roleCode = new RoleCode(command.code().toLowerCase(Locale.ROOT));

        if (roleRepository.existsByCode(roleCode)) {
            throw new RoleCodeAlreadyExistsException();
        }

        if (command.moduleId() != null && moduleRepository.findById(command.moduleId()) == null) {
            throw new ModuleNotFoundException();
        }

        Role role = new Role(
            UuidCreator.getTimeOrderedEpoch(),
            roleCode,
            command.moduleId(),
            command.description(),
            command.label(),
            true,
            Instant.now()
        );

        Role saved = roleRepository.save(role);

        return new CreateRoleResponse(
            saved.getId(),
            saved.getCode().value(),
            saved.getLabel(),
            saved.getModuleId(),
            saved.getDescription(),
            saved.isActive(),
            saved.getCreatedAt()
        );
    }
}
