package br.com.deltaglobalbank.delta_secure.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "termo_adesao_references")
public class TermoAdesaoReferenceEntity {

    @Id
    private UUID id;

    @Column(name = "ticket", nullable = false, unique = true)
    private String ticket;

    @Column(name = "hero_seguros_id", nullable = false)
    private int heroSegurosId;

    @Column(name = "convenio", nullable = false)
    private String convenio;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TermoAdesaoReferenceEntity() {
    }

    public TermoAdesaoReferenceEntity(
        UUID id,
        String ticket,
        int heroSegurosId,
        String convenio,
        String externalId,
        Instant createdAt
    ) {
        this.id = id;
        this.ticket = ticket;
        this.heroSegurosId = heroSegurosId;
        this.convenio = convenio;
        this.externalId = externalId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTicket() {
        return ticket;
    }

    public int getHeroSegurosId() {
        return heroSegurosId;
    }

    public String getConvenio() {
        return convenio;
    }

    public String getExternalId() {
        return externalId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
