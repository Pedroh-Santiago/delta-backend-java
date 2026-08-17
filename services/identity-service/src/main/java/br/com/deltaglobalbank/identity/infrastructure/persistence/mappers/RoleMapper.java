package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.RoleEntity;

public final class RoleMapper {

    private RoleMapper() {
    }

    public static Role toDomain(RoleEntity entity) {
        return new Role(
            entity.getId(),
            new RoleCode(entity.getCode()),
            entity.getModuleId(),
            entity.getDescription(),
            entity.getCreatedAt()
        );
    }

    public static RoleEntity toEntity(Role role) {
        RoleSnapshot s = role.snapshot();
        return new RoleEntity(
            s.id(),
            s.code().value(),
            s.moduleId(),
            s.description(),
            s.createdAt()
        );
    }
}
