package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.Module;
import br.com.deltaglobalbank.identity.domain.module.ModuleCode;
import br.com.deltaglobalbank.identity.domain.module.ModuleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.ModuleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.ModuleMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaModuleRepository;
import org.springframework.stereotype.Component;

@Component
public class ModuleRepositoryAdapter implements ModuleRepository {

    private final JpaModuleRepository jpaModuleRepository;

    public ModuleRepositoryAdapter(JpaModuleRepository jpaModuleRepository) {
        this.jpaModuleRepository = jpaModuleRepository;
    }

    @Override
    public Module findById(UUID id) {
        return jpaModuleRepository.findById(id).map(ModuleMapper::toDomain).orElse(null);
    }

    @Override
    public Module findByCode(ModuleCode code) {
        ModuleEntity entity = jpaModuleRepository.findByCode(code.value());
        return entity != null ? ModuleMapper.toDomain(entity) : null;
    }

    @Override
    public List<Module> findAll() {
        return jpaModuleRepository.findAll().stream().map(ModuleMapper::toDomain).toList();
    }

    @Override
    public List<Module> findAllByIds(Set<UUID> ids) {
        return jpaModuleRepository.findAllById(ids).stream().map(ModuleMapper::toDomain).toList();
    }
}
