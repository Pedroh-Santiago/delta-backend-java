package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.TenantIpAllowlistMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantIpAllowlistRepository;
import org.springframework.stereotype.Component;

@Component
public class TenantIpAllowlistRepositoryAdapter implements TenantIpAllowlistRepository {

    private final JpaTenantIpAllowlistRepository jpaTenantIpAllowlistRepository;

    public TenantIpAllowlistRepositoryAdapter(JpaTenantIpAllowlistRepository jpaTenantIpAllowlistRepository) {
        this.jpaTenantIpAllowlistRepository = jpaTenantIpAllowlistRepository;
    }

    @Override
    public TenantIpAllowlist findById(UUID id) {
        return jpaTenantIpAllowlistRepository.findById(id).map(TenantIpAllowlistMapper::toDomain).orElse(null);
    }

    @Override
    public TenantIpAllowlist save(TenantIpAllowlist entry) {
        return TenantIpAllowlistMapper.toDomain(
            jpaTenantIpAllowlistRepository.save(TenantIpAllowlistMapper.toEntity(entry))
        );
    }

    @Override
    public List<TenantIpAllowlist> findAllByTenantId(UUID tenantId) {
        return jpaTenantIpAllowlistRepository.findAllByTenantId(tenantId).stream()
            .map(TenantIpAllowlistMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByCidrAndTenantId(String cidr, UUID tenantId) {
        return jpaTenantIpAllowlistRepository.existsByCidrAndTenantId(cidr, tenantId);
    }

    @Override
    public void delete(UUID id) {
        jpaTenantIpAllowlistRepository.deleteById(id);
    }
}
