package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.deleteTenantIpAllowlist;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistEntryNotFoundException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteTenantIpAllowlistUseCase {

    private final TenantIpAllowlistRepository tenantIpAllowlistRepository;
    private final TenantIpAllowlistCache tenantIpAllowlistCache;

    public DeleteTenantIpAllowlistUseCase(
        TenantIpAllowlistRepository tenantIpAllowlistRepository,
        TenantIpAllowlistCache tenantIpAllowlistCache
    ) {
        this.tenantIpAllowlistRepository = tenantIpAllowlistRepository;
        this.tenantIpAllowlistCache = tenantIpAllowlistCache;
    }

    @Transactional
    public void execute(DeleteTenantIpAllowlistCommand command) {
        TenantIpAllowlist entry = tenantIpAllowlistRepository.findById(command.id());
        if (entry == null) {
            throw new IpAllowlistEntryNotFoundException();
        }
        if (!entry.getTenantId().equals(command.tenantId())) {
            throw new IpAllowlistEntryNotFoundException();
        }

        tenantIpAllowlistRepository.delete(command.id());
        tenantIpAllowlistCache.invalidate(entry.getTenantId());
    }
}
