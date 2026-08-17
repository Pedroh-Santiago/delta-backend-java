package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.InternalTransferenceEntity;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.InternalTransferenceMappers;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories.JpaInternalTransferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class InternalTransferenceAdapter implements InternalTransferenceRepository {

    private final JpaInternalTransferenceRepository internalTransferenceRepository;

    public InternalTransferenceAdapter(JpaInternalTransferenceRepository internalTransferenceRepository) {
        this.internalTransferenceRepository = internalTransferenceRepository;
    }

    @Override
    public InternalTransference save(InternalTransference internalTransference) {
        return InternalTransferenceMappers.toDomain(
            internalTransferenceRepository.save(InternalTransferenceMappers.toEntity(internalTransference))
        );
    }

    @Override
    public Page<InternalTransference> findByStatus(PaymentsStatus status, Pageable pageable) {
        return internalTransferenceRepository.findByStatus(status.name(), pageable)
            .map(InternalTransferenceMappers::toDomain);
    }

    @Override
    public Page<InternalTransference> findAll(Pageable pageable) {
        return internalTransferenceRepository.findAll(pageable).map(InternalTransferenceMappers::toDomain);
    }

    @Override
    public InternalTransference findById(UUID id) {
        InternalTransferenceEntity entity = internalTransferenceRepository.findById(id).orElse(null);
        return entity == null ? null : InternalTransferenceMappers.toDomain(entity);
    }
}
