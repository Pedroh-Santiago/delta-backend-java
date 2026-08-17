package br.com.deltaglobalbank.identity.domain.module;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ModuleRepository {
    Module findById(UUID id);

    Module findByCode(ModuleCode code);

    List<Module> findAll();

    List<Module> findAllByIds(Set<UUID> ids);
}
