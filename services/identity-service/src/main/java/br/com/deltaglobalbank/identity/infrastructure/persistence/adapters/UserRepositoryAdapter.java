package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.UserMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User findById(UUID id) {
        return jpaUserRepository.findById(id).map(UserMapper::toDomain).orElse(null);
    }

    @Override
    public User findByEmail(Email email) {
        UserEntity entity = jpaUserRepository.findByEmail(email.value());
        return entity != null ? UserMapper.toDomain(entity) : null;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaUserRepository.existsByEmail(email.value());
    }

    @Override
    public User save(User user) {
        UserEntity existing = jpaUserRepository.findById(user.getId()).orElse(null);
        UserEntity entityToSave = existing != null
            ? UserMapper.applyTo(user, existing)
            : UserMapper.toEntity(user);
        return UserMapper.toDomain(jpaUserRepository.save(entityToSave));
    }

    @Override
    public Page<User> findPage(Pageable pageable) {
        return jpaUserRepository.findAll(pageable).map(UserMapper::toDomain);
    }

    @Override
    public Page<User> findPageByTenantId(UUID tenantId, Pageable pageable) {
        return jpaUserRepository.findAllByTenantId(tenantId, pageable).map(UserMapper::toDomain);
    }

    @Override
    public void delete(User user) {
        jpaUserRepository.deleteById(user.getId());
    }
}
