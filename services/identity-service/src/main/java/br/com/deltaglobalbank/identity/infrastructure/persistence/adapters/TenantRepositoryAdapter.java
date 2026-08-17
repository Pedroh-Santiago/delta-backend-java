package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.TenantMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class TenantRepositoryAdapter implements TenantRepository {

    private final JpaTenantRepository jpaTenantRepository;

    public TenantRepositoryAdapter(JpaTenantRepository jpaTenantRepository) {
        this.jpaTenantRepository = jpaTenantRepository;
    }

    @Override
    public Tenant findById(UUID id) {
        return jpaTenantRepository.findById(id).map(TenantMapper::toDomain).orElse(null);
    }

    @Override
    public Tenant findBySlug(String slug) {
        TenantEntity entity = jpaTenantRepository.findBySlug(slug);
        return entity != null ? TenantMapper.toDomain(entity) : null;
    }

    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity existing = jpaTenantRepository.findById(tenant.getId()).orElse(null);
        TenantEntity entityToSave = existing != null
            ? TenantMapper.applyTo(tenant, existing)
            : TenantMapper.toEntity(tenant);
        return TenantMapper.toDomain(jpaTenantRepository.save(entityToSave));
    }

    @Override
    public void delete(UUID id) {
        jpaTenantRepository.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaTenantRepository.existsBySlug(slug);
    }

    @Override
    public Page<Tenant> findPage(Pageable pageable) {
        return jpaTenantRepository.findAll(pageable).map(TenantMapper::toDomain);
    }
}
