package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.InternalTransferenceEntity;

public final class InternalTransferenceMappers {

    private InternalTransferenceMappers() {
    }

    public static InternalTransference toDomain(InternalTransferenceEntity entity) {
        return new InternalTransference(
            entity.getId(),
            entity.getAccountNumber(),
            entity.getPayerId(),
            entity.getPaidAt(),
            entity.getAmount(),
            PaymentsStatus.valueOf(entity.getStatus()),
            entity.getDescription(),
            entity.getRequestedAt(),
            entity.getRequestedById()
        );
    }

    public static InternalTransferenceEntity toEntity(InternalTransference domain) {
        return new InternalTransferenceEntity(
            domain.id(),
            domain.requestedById(),
            domain.requestedAt(),
            domain.payerId(),
            domain.paidAt(),
            domain.accountNumber(),
            domain.amount(),
            domain.description(),
            domain.status().name()
        );
    }
}
