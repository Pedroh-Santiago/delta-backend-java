package br.com.deltaglobalbank.identity.domain.user

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import java.time.Instant
import java.util.UUID

class User(
    val id: UUID,
    val tenantId: UUID,
    val fullName: String,
    val email: Email,
    passwordHash: HashedPassword,
    status: UserStatus,
    mustChangePassword: Boolean,
    passwordChangedAt: Instant?,
    lastLoginAt: Instant?,
    failedAttempts: Int,
    lockedUntil: Instant?,
    val createdAt: Instant,
    updatedAt: Instant
) {

    private var _passwordHash: HashedPassword = passwordHash
    private var _status: UserStatus = status
    private var _mustChangePassword: Boolean = mustChangePassword
    private var _passwordChangedAt: Instant? = passwordChangedAt
    private var _lastLoginAt: Instant? = lastLoginAt
    private var _failedAttempts: Int = failedAttempts
    private var _lockedUntil: Instant? = lockedUntil
    private var _updatedAt: Instant = updatedAt

    fun authenticate(
        rawPassword: String,
        hasher: PasswordHasher
    ) {

        if (!hasher.matches(rawPassword, _passwordHash)) {
            registerFailedLoginAttempt()
            throw InvalidCredentialsException()
        }

        if (_status != UserStatus.ACTIVE) {
            throw UserNotActiveException()
        }

        registerSuccessfulLogin()
    }

    fun changePassword(
        currentPassword: Password,
        newPassword: Password,
        hasher: PasswordHasher
    ) {
        if (!hasher.matches(currentPassword.value, _passwordHash)) {
            throw CurrentPasswordIncorrectException()
        }
        if (currentPassword.value == newPassword.value) {
            throw NewPasswordSameAsCurrentException()
        }

        val now = Instant.now()
        _passwordHash = hasher.hash(newPassword)
        _mustChangePassword = false
        _passwordChangedAt = now
        _updatedAt = now
    }

    private fun registerFailedLoginAttempt() {
        _failedAttempts += 1
        _updatedAt = Instant.now()
        // TODO: implementar lockout antes de produção
    }

    private fun registerSuccessfulLogin() {
        val now = Instant.now()
        _failedAttempts = 0
        _lockedUntil = null
        _lastLoginAt = now
        _updatedAt = now
    }

    fun mustChangePassword(): Boolean = _mustChangePassword

    fun isActive(): Boolean = _status == UserStatus.ACTIVE

    fun activate(){
        _status = UserStatus.ACTIVE
        _updatedAt = Instant.now()
    }

    fun isSuspended(): Boolean = _status == UserStatus.SUSPENDED

    fun suspend() {
        _status = UserStatus.SUSPENDED
        _updatedAt = Instant.now()
    }

    fun snapshot(): UserSnapshot = UserSnapshot(
        id = id,
        tenantId = tenantId,
        fullName = fullName,
        email = email,
        passwordHash = _passwordHash,
        status = _status,
        mustChangePassword = _mustChangePassword,
        passwordChangedAt = _passwordChangedAt,
        lastLoginAt = _lastLoginAt,
        failedAttempts = _failedAttempts,
        lockedUntil = _lockedUntil,
        createdAt = createdAt,
        updatedAt = _updatedAt
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "User(id=$id, email=$email, status=$_status)"

    companion object {
        fun newUser(
            id: UUID,
            tenantId: UUID,
            fullName: String,
            email: Email,
            passwordHash: HashedPassword,
            mustChangePassword: Boolean = true
        ): User {
            val now = Instant.now()
            return User(
                id = id,
                tenantId = tenantId,
                fullName = fullName,
                email = email,
                passwordHash = passwordHash,
                status = UserStatus.ACTIVE,
                mustChangePassword = mustChangePassword,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 0,
                lockedUntil = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

data class UserSnapshot(
    val id: UUID,
    val tenantId: UUID,
    val fullName: String,
    val email: Email,
    val passwordHash: HashedPassword,
    val status: UserStatus,
    val mustChangePassword: Boolean,
    val passwordChangedAt: Instant?,
    val lastLoginAt: Instant?,
    val failedAttempts: Int,
    val lockedUntil: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
