package br.com.deltaglobalbank.identity.features.roles.listRoles;

import java.util.Comparator;
import java.util.List;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListRolesUseCase {

    private static final String PLATFORM_ADMIN = "platform.admin";

    private final RoleRepository roleRepository;

    public ListRolesUseCase(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public ListRolesResponse execute(ListRolesQuery query) {
        List<ListedRole> items = roleRepository.findAll().stream()
            .filter(it -> query.includePlatformAdmin() || !it.getCode().value().equals(PLATFORM_ADMIN))
            .sorted(Comparator.comparing(it -> it.getCode().value()))
            .map(this::toListed)
            .toList();

        return new ListRolesResponse(items);
    }

    private ListedRole toListed(Role role) {
        String description = role.getDescription();
        String name = description != null && !description.isBlank() ? description : role.getCode().value();
        return new ListedRole(role.getCode().value(), name, description);
    }
}
