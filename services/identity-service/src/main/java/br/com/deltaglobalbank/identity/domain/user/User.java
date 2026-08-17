package br.com.deltaglobalbank.identity.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;

public class User {

    private final UUID id;
    private final UUID tenantId;
    private final Instant createdAt;

    private String fullName;
    private Email email;
    private HashedPassword passwordHash;
    private UserStatus status;
    private boolean mustChangePassword;
    private Instant passwordChangedAt;
    private Instant lastLoginAt;
    private int failedAttempts;
    private Instant lockedUntil;
    private Instant updatedAt;

    public User(
        UUID id,
        UUID tenantId,
        String fullName,
        Email email,
        HashedPassword passwordHash,
        UserStatus status,
        boolean mustChangePassword,
        Instant passwordChangedAt,
        Instant lastLoginAt,
        int failedAttempts,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.mustChangePassword = mustChangePassword;
        this.passwordChangedAt = passwordChangedAt;
        this.lastLoginAt = lastLoginAt;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void authenticate(String rawPassword, PasswordHasher hasher, Instant now, LockoutPolicy policy) {
        if (policy.enabled() && status == UserStatus.LOCKED) {
            if (lockedUntil == null) {
                throw new UserLockedException(null);
            }
            if (now.isBefore(lockedUntil)) {
                throw new UserLockedException(lockedUntil);
            }
            clearLockout();
        }

        if (!hasher.matches(rawPassword, passwordHash)) {
            registerFailedLoginAttempt(now, policy);
            throw new InvalidCredentialsException();
        }

        if (status == UserStatus.SUSPENDED) {
            throw new UserNotActiveException();
        }

        registerSuccessfulLogin();
    }

    public void changePassword(Password currentPassword, Password newPassword, PasswordHasher hasher) {
        if (!hasher.matches(currentPassword.value(), passwordHash)) {
            throw new CurrentPasswordIncorrectException();
        }
        if (currentPassword.value().equals(newPassword.value())) {
            throw new NewPasswordSameAsCurrentException();
        }

        Instant now = Instant.now();
        passwordHash = hasher.hash(newPassword);
        mustChangePassword = false;
        passwordChangedAt = now;
        updatedAt = now;
    }

    private void registerFailedLoginAttempt(Instant now, LockoutPolicy policy) {
        failedAttempts += 1;
        if (policy.enabled()) {
            int level = policy.cumulativeThresholds().indexOf(failedAttempts);
            if (level >= 0) {
                status = UserStatus.LOCKED;
                lockedUntil = level < policy.durations().size()
                    ? now.plus(policy.durations().get(level))
                    : null;
            }
        }
        updatedAt = now;
    }

    private void registerSuccessfulLogin() {
        Instant now = Instant.now();
        failedAttempts = 0;
        lockedUntil = null;
        status = UserStatus.ACTIVE;
        lastLoginAt = now;
        updatedAt = now;
    }

    private void clearLockout() {
        lockedUntil = null;
        status = UserStatus.ACTIVE;
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void activate() {
        status = UserStatus.ACTIVE;
        updatedAt = Instant.now();
    }

    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    public void suspend() {
        status = UserStatus.SUSPENDED;
        updatedAt = Instant.now();
    }

    public void resetPassword(HashedPassword newHash) {
        Instant now = Instant.now();
        passwordHash = newHash;
        mustChangePassword = true;
        passwordChangedAt = now;
        failedAttempts = 0;
        lockedUntil = null;
        if (status == UserStatus.LOCKED) {
            status = UserStatus.ACTIVE;
        }
        updatedAt = now;
    }

    public boolean updateProfile(String fullName, Email email) {
        if (fullName.equals(this.fullName) && email.equals(this.email)) {
            return false;
        }
        this.fullName = fullName;
        this.email = email;
        updatedAt = Instant.now();
        return true;
    }

    public UserSnapshot snapshot() {
        return new UserSnapshot(
            id,
            tenantId,
            fullName,
            email,
            passwordHash,
            status,
            mustChangePassword,
            passwordChangedAt,
            lastLoginAt,
            failedAttempts,
            lockedUntil,
            createdAt,
            updatedAt
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof User user && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User(id=" + id + ", email=" + email + ", status=" + status + ")";
    }

    public static User newUser(
        UUID id,
        UUID tenantId,
        String fullName,
        Email email,
        HashedPassword passwordHash
    ) {
        return newUser(id, tenantId, fullName, email, passwordHash, true);
    }

    public static User newUser(
        UUID id,
        UUID tenantId,
        String fullName,
        Email email,
        HashedPassword passwordHash,
        boolean mustChangePassword
    ) {
        Instant now = Instant.now();
        return new User(
            id,
            tenantId,
            fullName,
            email,
            passwordHash,
            UserStatus.ACTIVE,
            mustChangePassword,
            null,
            null,
            0,
            null,
            now,
            now
        );
    }
}
