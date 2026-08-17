package br.com.deltaglobalbank.identity.domain.user;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User findById(UUID id);

    User findByEmail(Email email);

    User save(User user);

    boolean existsByEmail(Email email);

    Page<User> findPage(Pageable pageable);

    Page<User> findPageByTenantId(UUID tenantId, Pageable pageable);

    void delete(User user);
}
