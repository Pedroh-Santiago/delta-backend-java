package br.com.deltaglobalbank.delta_secure.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import br.com.deltaglobalbank.delta_secure.infrastructure.persistence.entities.TermoAdesaoReferenceEntity;
import br.com.deltaglobalbank.delta_secure.infrastructure.persistence.mappers.TermoAdesaoReferenceMappers;
import br.com.deltaglobalbank.delta_secure.infrastructure.persistence.repositories.JpaTermoAdesaoReferenceRepository;
import org.springframework.stereotype.Component;

@Component
public class TermoAdesaoReferenceRepositoryAdapter implements TermoAdesaoReferenceRepository {

    private final JpaTermoAdesaoReferenceRepository jpa;

    public TermoAdesaoReferenceRepositoryAdapter(JpaTermoAdesaoReferenceRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TermoAdesaoReference save(TermoAdesaoReference reference) {
        TermoAdesaoReferenceEntity saved = jpa.save(TermoAdesaoReferenceMappers.toEntity(reference));
        return TermoAdesaoReferenceMappers.toDomain(saved);
    }

    @Override
    public TermoAdesaoReference findByTicket(String ticket) {
        TermoAdesaoReferenceEntity entity = jpa.findByTicket(ticket);
        return entity != null ? TermoAdesaoReferenceMappers.toDomain(entity) : null;
    }
}
