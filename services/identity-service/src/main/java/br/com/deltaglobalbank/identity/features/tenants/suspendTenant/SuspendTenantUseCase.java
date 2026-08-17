package br.com.deltaglobalbank.identity.features.tenants.suspendTenant;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuspendTenantUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuspendTenantUseCase.class);

    private final TenantRepository tenantRepository;

    public SuspendTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public void suspendTenant(UUID tenantId, UUID actorId) {
        Tenant tenantExists = tenantRepository.findById(tenantId);
        if (tenantExists == null) {
            throw new TenantNotFoundException();
        }
        tenantExists.suspend();
        tenantRepository.save(tenantExists);
        log.info("tenant suspended tenantId={} by={}", tenantId, actorId);
    }
}
