package br.com.deltaglobalbank.identity.domain.user;

import java.time.Instant;

public final class UserLockedException extends UserDomainException {

    private final Instant lockedUntil;

    public UserLockedException(Instant lockedUntil) {
        super("user_locked");
        this.lockedUntil = lockedUntil;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}
