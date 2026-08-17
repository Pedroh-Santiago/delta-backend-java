package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.MakePixEntity;

public final class MakePixMapper {

    private MakePixMapper() {
    }

    public static MakePix toDomain(MakePixEntity entity) {
        return new MakePix(
            entity.getId(),
            entity.getAccountId(),
            entity.getRecipientInstitutionCode(),
            entity.getRecipientBranchCode(),
            entity.getRecipientAccountNumber(),
            PaymentsStatus.valueOf(entity.getStatus()),
            entity.getPaidAt(),
            entity.getRecipientAccountType(),
            entity.getRecipientName(),
            entity.getOperationAmount(),
            entity.getCreatedAt()
        );
    }

    public static MakePixEntity toEntity(MakePix domain) {
        return new MakePixEntity(
            domain.id(),
            domain.accountId(),
            domain.recipientInstitutionCode(),
            domain.recipientBranchCode(),
            domain.recipientAccountNumber(),
            domain.recipientAccountType(),
            domain.recipientName(),
            domain.operationAmount(),
            domain.paidAt(),
            domain.createdAt(),
            domain.status().name()
        );
    }
}
