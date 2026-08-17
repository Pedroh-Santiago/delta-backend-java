package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.module.TenantModule;
import br.com.deltaglobalbank.identity.domain.module.TenantModuleRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantModuleEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.TenantModuleMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantModuleRepository;
import org.springframework.stereotype.Component;

@Component
public class TenantModuleRepositoryAdapter implements TenantModuleRepository {

    private final JpaTenantModuleRepository jpaTenantModuleRepository;

    public TenantModuleRepositoryAdapter(JpaTenantModuleRepository jpaTenantModuleRepository) {
        this.jpaTenantModuleRepository = jpaTenantModuleRepository;
    }

    @Override
    public List<TenantModule> findAllByTenantId(UUID tenantId) {
        return jpaTenantModuleRepository.findAllByTenantId(tenantId).stream()
            .map(TenantModuleMapper::toDomain)
            .toList();
    }

    @Override
    public List<TenantModule> findAllByTenantIdAndEnabled(UUID tenantId, boolean enabled) {
        return jpaTenantModuleRepository.findAllByTenantIdAndEnabled(tenantId, enabled).stream()
            .map(TenantModuleMapper::toDomain)
            .toList();
    }

    @Override
    public TenantModule findByTenantIdAndModuleId(UUID tenantId, UUID moduleId) {
        TenantModuleEntity entity = jpaTenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId);
        return entity != null ? TenantModuleMapper.toDomain(entity) : null;
    }

    @Override
    public TenantModule save(TenantModule tenantModule) {
        TenantModuleEntity existing = jpaTenantModuleRepository.findById(tenantModule.getId()).orElse(null);
        TenantModuleEntity entityToSave = existing != null
            ? TenantModuleMapper.applyTo(tenantModule, existing)
            : TenantModuleMapper.toEntity(tenantModule);
        return TenantModuleMapper.toDomain(jpaTenantModuleRepository.save(entityToSave));
    }
}
