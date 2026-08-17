package br.com.deltaglobalbank.identity.features.tenants.activateTenant;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateTenantUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateTenantUseCase.class);

    private final TenantRepository tenantRepository;

    public ActivateTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public void activateTenant(UUID tenantId, UUID actorId) {
        Tenant tenantExists = tenantRepository.findById(tenantId);
        if (tenantExists == null) {
            throw new TenantNotFoundException();
        }
        tenantExists.activate();
        tenantRepository.save(tenantExists);
        log.info("tenant activated tenantId={} by={}", tenantId, actorId);
    }
}
