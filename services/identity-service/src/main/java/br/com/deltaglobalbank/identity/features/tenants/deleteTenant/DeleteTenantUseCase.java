package br.com.deltaglobalbank.identity.features.tenants.deleteTenant;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteTenantUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteTenantUseCase.class);

    private final TenantRepository tenantRepository;

    public DeleteTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public void deleteTenant(UUID tenantId, UUID actorId) {
        Tenant tenantExists = tenantRepository.findById(tenantId);
        if (tenantExists == null) {
            throw new TenantNotFoundException();
        }
        tenantExists.deactivate();
        tenantRepository.save(tenantExists);
        tenantRepository.delete(tenantId);
        log.info("tenant deleted tenantId={} by={}", tenantId, actorId);
    }
}
