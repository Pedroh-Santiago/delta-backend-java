package br.com.deltaglobalbank.identity.domain.tenant;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantRepository {
    Tenant findById(UUID id);

    Tenant findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Tenant> findPage(Pageable pageable);

    Tenant save(Tenant tenant);

    void delete(UUID id);
}
