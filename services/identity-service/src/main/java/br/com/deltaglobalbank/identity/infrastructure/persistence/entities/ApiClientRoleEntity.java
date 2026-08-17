package br.com.deltaglobalbank.identity.infrastructure.persistence.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "api_client_roles")
@SQLDelete(sql = "UPDATE identity.api_client_roles SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ApiClientRoleEntity {

    @Id
    private UUID id;

    @Column(name = "api_client_id", nullable = false)
    private UUID apiClientId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ApiClientRoleEntity() {
    }

    public ApiClientRoleEntity(UUID id, UUID apiClientId, UUID roleId, Instant grantedAt, Instant deletedAt) {
        this.id = id;
        this.apiClientId = apiClientId;
        this.roleId = roleId;
        this.grantedAt = grantedAt;
        this.deletedAt = deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApiClientId() {
        return apiClientId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
