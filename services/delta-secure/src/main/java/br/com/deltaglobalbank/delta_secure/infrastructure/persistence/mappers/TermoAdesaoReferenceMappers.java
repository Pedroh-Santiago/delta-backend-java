package br.com.deltaglobalbank.delta_secure.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.infrastructure.persistence.entities.TermoAdesaoReferenceEntity;

public final class TermoAdesaoReferenceMappers {

    private TermoAdesaoReferenceMappers() {
    }

    public static TermoAdesaoReferenceEntity toEntity(TermoAdesaoReference domain) {
        return new TermoAdesaoReferenceEntity(
            domain.id(),
            domain.ticket(),
            domain.heroSegurosId(),
            domain.convenio().name(),
            domain.externalId(),
            domain.createdAt()
        );
    }

    public static TermoAdesaoReference toDomain(TermoAdesaoReferenceEntity entity) {
        return new TermoAdesaoReference(
            entity.getId(),
            entity.getTicket(),
            entity.getHeroSegurosId(),
            Convenio.valueOf(entity.getConvenio()),
            entity.getExternalId(),
            entity.getCreatedAt()
        );
    }
}
