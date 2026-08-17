package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleSnapshot;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity;

public final class ModuleMapper {

    private ModuleMapper() {
    }

    public static Module toDomain(ModuleEntity entity) {
        return new Module(
            entity.getId(),
            new ModuleCode(entity.getCode()),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedAt()
        );
    }

    public static ModuleEntity toEntity(Module module) {
        ModuleSnapshot s = module.snapshot();
        return new ModuleEntity(
            s.id(),
            s.code().value(),
            s.name(),
            s.description(),
            s.createdAt()
        );
    }
}
