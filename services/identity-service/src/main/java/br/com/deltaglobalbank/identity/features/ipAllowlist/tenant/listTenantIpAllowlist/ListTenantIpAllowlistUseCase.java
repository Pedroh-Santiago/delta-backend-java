package br.com.deltaglobalbank.identity.features.ipAllowlist.tenant.listTenantIpAllowlist;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlist;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.TenantIpAllowlistRepository;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTenantIpAllowlistUseCase {

    private final TenantIpAllowlistRepository tenantIpAllowlistRepository;
    private final TenantRepository tenantRepository;

    public ListTenantIpAllowlistUseCase(
        TenantIpAllowlistRepository tenantIpAllowlistRepository,
        TenantRepository tenantRepository
    ) {
        this.tenantIpAllowlistRepository = tenantIpAllowlistRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public ListTenantIpAllowlistResponse execute(ListTenantIpAllowlistQuery query) {
        if (tenantRepository.findById(query.tenantId()) == null) {
            throw new TenantNotFoundException();
        }

        List<TenantIpAllowlist> entries = tenantIpAllowlistRepository.findAllByTenantId(query.tenantId());
        List<ListedTenantIpAllowlist> items = entries.stream()
            .map(it -> new ListedTenantIpAllowlist(
                it.getId(), it.getTenantId(), it.getCidr(), it.getDescription(), it.getCreatedAt()))
            .toList();

        return new ListTenantIpAllowlistResponse(items);
    }
}
