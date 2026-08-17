package br.com.deltaglobalbank.customers.infrastructure.persistence.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_issuers")
public class DocumentIssuerEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    protected DocumentIssuerEntity() {
    }

    public DocumentIssuerEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
