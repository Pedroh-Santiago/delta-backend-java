package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers;

import br.com.deltaglobalbank.identity.domain.user.Email;
import br.com.deltaglobalbank.identity.domain.user.HashedPassword;
import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserSnapshot;
import br.com.deltaglobalbank.identity.domain.user.UserStatus;
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getTenantId(),
            entity.getFullName(),
            new Email(entity.getEmail()),
            new HashedPassword(entity.getPasswordHash()),
            UserStatus.fromDatabaseValue(entity.getStatus()),
            entity.isMustChangePassword(),
            entity.getPasswordChangedAt(),
            entity.getLastLoginAt(),
            entity.getFailedAttempts(),
            entity.getLockedUntil(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static UserEntity toEntity(User user) {
        UserSnapshot s = user.snapshot();
        return new UserEntity(
            s.id(),
            s.tenantId(),
            s.fullName(),
            s.email().value(),
            s.passwordHash().value(),
            s.status().toDatabaseValue(),
            s.mustChangePassword(),
            s.passwordChangedAt(),
            s.lastLoginAt(),
            s.failedAttempts(),
            s.lockedUntil(),
            s.createdAt(),
            s.updatedAt(),
            null
        );
    }

    public static UserEntity applyTo(User user, UserEntity entity) {
        UserSnapshot s = user.snapshot();
        entity.setFullName(s.fullName());
        entity.setEmail(s.email().value());
        entity.setPasswordHash(s.passwordHash().value());
        entity.setStatus(s.status().toDatabaseValue());
        entity.setMustChangePassword(s.mustChangePassword());
        entity.setPasswordChangedAt(s.passwordChangedAt());
        entity.setLastLoginAt(s.lastLoginAt());
        entity.setFailedAttempts(s.failedAttempts());
        entity.setLockedUntil(s.lockedUntil());
        entity.setUpdatedAt(s.updatedAt());
        return entity;
    }
}
