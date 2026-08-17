package br.com.deltaglobalbank.customers.domain.document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;

public class PersonalDocument {

    private final UUID id;
    private final UUID customerId;
    private final DocumentType documentType;
    private final String documentNumber;
    private final UUID issuerId;
    private final Uf issuerState;
    private final LocalDate expiresAt;
    private final LocalDate issuedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private PersonalDocument(
        UUID id,
        UUID customerId,
        DocumentType documentType,
        String documentNumber,
        UUID issuerId,
        Uf issuerState,
        LocalDate expiresAt,
        LocalDate issuedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "personal_document_id_required");
        this.customerId = Objects.requireNonNull(customerId, "personal_document_customer_id_required");
        this.documentType = Objects.requireNonNull(documentType, "personal_document_type_required");
        this.documentNumber = Objects.requireNonNull(documentNumber, "personal_document_number_required");
        this.issuerId = Objects.requireNonNull(issuerId, "personal_document_issuer_id_required");
        this.issuerState = Objects.requireNonNull(issuerState, "personal_document_issuer_state_required");
        this.expiresAt = expiresAt;
        this.issuedAt = Objects.requireNonNull(issuedAt, "personal_document_issued_at_required");
        this.createdAt = Objects.requireNonNull(createdAt, "personal_document_created_at_required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "personal_document_updated_at_required");
    }

    public static PersonalDocument create(
        UUID id,
        UUID customerId,
        DocumentType documentType,
        String documentNumber,
        UUID issuerId,
        Uf issuerState,
        LocalDate expiresAt,
        LocalDate issuedAt
    ) {
        Instant now = Instant.now();
        return new PersonalDocument(
            id,
            customerId,
            documentType,
            documentNumber,
            issuerId,
            issuerState,
            expiresAt,
            issuedAt,
            now,
            now
        );
    }

    public static PersonalDocument restore(
        UUID id,
        UUID customerId,
        DocumentType documentType,
        String documentNumber,
        UUID issuerId,
        Uf issuerState,
        LocalDate expiresAt,
        LocalDate issuedAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new PersonalDocument(
            id,
            customerId,
            documentType,
            documentNumber,
            issuerId,
            issuerState,
            expiresAt,
            issuedAt,
            createdAt,
            updatedAt
        );
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public DocumentType documentType() {
        return documentType;
    }

    public String documentNumber() {
        return documentNumber;
    }

    public UUID issuerId() {
        return issuerId;
    }

    public Uf issuerState() {
        return issuerState;
    }

    public LocalDate expiresAt() {
        return expiresAt;
    }

    public LocalDate issuedAt() {
        return issuedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<Object> identity() {
        return Arrays.asList(documentType, documentNumber);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PersonalDocument document && document.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
