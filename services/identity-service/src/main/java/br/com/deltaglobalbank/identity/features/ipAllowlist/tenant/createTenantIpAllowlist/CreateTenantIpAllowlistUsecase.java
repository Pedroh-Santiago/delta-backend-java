package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.createTenantIpAllowlist;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.CidrAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.infrastructure.security.ipAllowlist.TenantIpAllowlistCache;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CreateTenantIpAllowlistUsecase {

    private final TenantIpAllowlistRepository tenantIpAllowlistRepository;
    private final TenantRepository tenantRepository;
    private final TenantIpAllowlistCache tenantIpAllowlistCache;

    public CreateTenantIpAllowlistUsecase(
        TenantIpAllowlistRepository tenantIpAllowlistRepository,
        TenantRepository tenantRepository,
        TenantIpAllowlistCache tenantIpAllowlistCache
    ) {
        this.tenantIpAllowlistRepository = tenantIpAllowlistRepository;
        this.tenantRepository = tenantRepository;
        this.tenantIpAllowlistCache = tenantIpAllowlistCache;
    }

    @Transactional
    public CreateTenantIpAllowlistResponse execute(CreateTenantIpAllowlistCommand command) {
        Tenant tenant = tenantRepository.findById(command.tenantId());
        if (tenant == null) {
            throw new TenantNotFoundException();
        }
        if (!tenant.isActive()) {
            throw new TenantInactiveException();
        }

        String cidr = new Cidr(command.cidr()).getNetworkRangeIp();
        if (tenantIpAllowlistRepository.existsByCidrAndTenantId(cidr, command.tenantId())) {
            throw new CidrAlreadyExistsException();
        }

        TenantIpAllowlist tenantIpAllowlist = TenantIpAllowlist.newTenantIpAllowlist(
            UuidCreator.getTimeOrderedEpoch(),
            command.tenantId(),
            new Cidr(cidr),
            command.description()
        );

        tenantIpAllowlistRepository.save(tenantIpAllowlist);
        tenantIpAllowlistCache.invalidate(command.tenantId());

        return new CreateTenantIpAllowlistResponse(
            tenantIpAllowlist.getId(),
            tenantIpAllowlist.getTenantId(),
            tenantIpAllowlist.getCidr(),
            tenantIpAllowlist.getDescription(),
            tenantIpAllowlist.getCreatedAt()
        );
    }
}
