package br.com.deltaglobalbank.customers.infrastructure.persistence.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "customer_documents")
@SQLDelete(sql = "UPDATE customers.customer_documents SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PersonalDocumentEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "issuer_id", nullable = false)
    private UUID issuerId;

    @Column(name = "issuer_state", nullable = false)
    private String issuerState;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected PersonalDocumentEntity() {
    }

    public PersonalDocumentEntity(
        UUID id,
        UUID customerId,
        String documentType,
        String documentNumber,
        UUID issuerId,
        String issuerState,
        LocalDate issuedAt,
        LocalDate expiresAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.issuerId = issuerId;
        this.issuerState = issuerState;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public UUID getIssuerId() {
        return issuerId;
    }

    public String getIssuerState() {
        return issuerState;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
