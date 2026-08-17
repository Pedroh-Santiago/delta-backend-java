package br.com.deltaglobalbank.identity.infrastructure.persistence.mappers

import br.com.deltaglobalbank.identity.domain.user.Email
import br.com.deltaglobalbank.identity.domain.user.HashedPassword
import br.com.deltaglobalbank.identity.domain.user.User
import br.com.deltaglobalbank.identity.domain.user.UserStatus
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.UserEntity

fun UserEntity.toDomain(): User = User(
    id = this.id,
    tenantId = this.tenantId,
    fullName = this.fullName,
    email = Email(this.email),
    passwordHash = HashedPassword(this.passwordHash),
    status = UserStatus.fromDatabaseValue(this.status),
    mustChangePassword = this.mustChangePassword,
    passwordChangedAt = this.passwordChangedAt,
    lastLoginAt = this.lastLoginAt,
    failedAttempts = this.failedAttempts,
    lockedUntil = this.lockedUntil,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun User.toEntity(): UserEntity {
    val s = snapshot()
    return UserEntity(
        id = s.id,
        tenantId = s.tenantId,
        fullName = s.fullName,
        email = s.email.value,
        passwordHash = s.passwordHash.value,
        status = s.status.toDatabaseValue(),
        mustChangePassword = s.mustChangePassword,
        passwordChangedAt = s.passwordChangedAt,
        lastLoginAt = s.lastLoginAt,
        failedAttempts = s.failedAttempts,
        lockedUntil = s.lockedUntil,
        createdAt = s.createdAt,
        updatedAt = s.updatedAt
    )
}

fun User.applyTo(entity: UserEntity): UserEntity {
    val s = snapshot()
    entity.fullName = s.fullName
    entity.email = s.email.value
    entity.passwordHash = s.passwordHash.value
    entity.status = s.status.toDatabaseValue()
    entity.mustChangePassword = s.mustChangePassword
    entity.passwordChangedAt = s.passwordChangedAt
    entity.lastLoginAt = s.lastLoginAt
    entity.failedAttempts = s.failedAttempts
    entity.lockedUntil = s.lockedUntil
    entity.updatedAt = s.updatedAt
    return entity
}
