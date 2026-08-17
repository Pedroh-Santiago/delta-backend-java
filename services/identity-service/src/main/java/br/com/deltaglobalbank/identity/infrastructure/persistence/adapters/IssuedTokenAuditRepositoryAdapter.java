package br.com.deltaglobalbank.identity.infrastructure.persistence.adapters;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAudit;
import br.com.deltaglobalbank.identity.domain.token.IssuedTokenAuditRepository;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.IssuedTokenAuditEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.mappers.IssuedTokenAuditMapper;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaIssuedTokenAuditRepository;
import org.springframework.stereotype.Component;

@Component
public class IssuedTokenAuditRepositoryAdapter implements IssuedTokenAuditRepository {

    private final JpaIssuedTokenAuditRepository jpaIssuedTokenAuditRepository;

    public IssuedTokenAuditRepositoryAdapter(JpaIssuedTokenAuditRepository jpaIssuedTokenAuditRepository) {
        this.jpaIssuedTokenAuditRepository = jpaIssuedTokenAuditRepository;
    }

    @Override
    public void save(IssuedTokenAudit audit) {
        jpaIssuedTokenAuditRepository.save(IssuedTokenAuditMapper.toEntity(audit));
    }

    @Override
    public IssuedTokenAudit findByJti(String jti) {
        IssuedTokenAuditEntity entity = jpaIssuedTokenAuditRepository.findByJti(UUID.fromString(jti));
        return entity != null ? IssuedTokenAuditMapper.toDomain(entity) : null;
    }
}
