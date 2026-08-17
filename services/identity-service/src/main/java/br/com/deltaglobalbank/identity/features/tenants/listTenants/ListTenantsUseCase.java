package br.com.deltaglobalbank.identity.features.tenants.listTenants;

import java.util.List;

import br.com.deltaglobalbank.identity.domain.tenant.Tenant;
import br.com.deltaglobalbank.identity.domain.tenant.TenantRepository;
import br.com.deltaglobalbank.identity.domain.tenant.TenantSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTenantsUseCase {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final TenantRepository tenantRepository;

    public ListTenantsUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public ListTenantsResponse execute(ListTenantsQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Tenant> tenantsPage = tenantRepository.findPage(pageable);

        if (tenantsPage.getContent().isEmpty()) {
            return new ListTenantsResponse(List.of(), safePage, safeSize, 0, 0);
        }

        List<ListedTenant> items = tenantsPage.getContent().stream()
            .map(tenant -> {
                TenantSnapshot snapshot = tenant.snapshot();
                return new ListedTenant(
                    snapshot.id(),
                    snapshot.name(),
                    snapshot.slug(),
                    snapshot.status().toDatabaseValue(),
                    snapshot.createdAt()
                );
            })
            .toList();

        return new ListTenantsResponse(
            items,
            safePage,
            safeSize,
            tenantsPage.getTotalElements(),
            tenantsPage.getTotalPages()
        );
    }
}
