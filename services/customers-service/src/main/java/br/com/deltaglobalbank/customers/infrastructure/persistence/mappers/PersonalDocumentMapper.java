package br.com.deltaglobalbank.customers.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.customers.domain.document.DocumentType;
import br.com.deltaglobalbank.customers.domain.document.PersonalDocument;
import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.PersonalDocumentEntity;

public final class PersonalDocumentMapper {

    private PersonalDocumentMapper() {
    }

    public static PersonalDocument toDomain(PersonalDocumentEntity entity) {
        return PersonalDocument.restore(
            entity.getId(),
            entity.getCustomerId(),
            DocumentType.fromDatabaseValue(entity.getDocumentType()),
            entity.getDocumentNumber(),
            entity.getIssuerId(),
            new Uf(entity.getIssuerState()),
            entity.getExpiresAt(),
            entity.getIssuedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static PersonalDocumentEntity toEntity(PersonalDocument document) {
        return new PersonalDocumentEntity(
            document.id(),
            document.customerId(),
            document.documentType().toDatabaseValue(),
            document.documentNumber(),
            document.issuerId(),
            document.issuerState().value(),
            document.issuedAt(),
            document.expiresAt(),
            document.createdAt(),
            document.updatedAt()
        );
    }
}
