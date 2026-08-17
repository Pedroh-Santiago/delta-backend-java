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
@Table(name = "user_roles")
@SQLDelete(sql = "UPDATE identity.user_roles SET deleted_at = NOW() AT TIME ZONE 'UTC' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserRoleEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "granted_by")
    private UUID grantedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected UserRoleEntity() {
    }

    public UserRoleEntity(UUID id, UUID userId, UUID roleId, Instant grantedAt, UUID grantedBy, Instant deletedAt) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.grantedAt = grantedAt;
        this.grantedBy = grantedBy;
        this.deletedAt = deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public UUID getGrantedBy() {
        return grantedBy;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
