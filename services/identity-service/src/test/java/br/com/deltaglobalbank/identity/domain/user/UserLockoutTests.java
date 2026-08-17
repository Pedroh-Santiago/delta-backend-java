package br.com.deltaglobalbank.identity.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import org.junit.jupiter.api.Test;

class UserLockoutTests {

    private final Instant now = Instant.parse("2026-08-01T10:00:00Z");
    private final LockoutPolicy policy = new LockoutPolicy(
        true,
        List.of(5, 3, 2),
        List.of(Duration.ofMinutes(5), Duration.ofMinutes(15))
    );

    private User userWith(UserStatus status, int failedAttempts, Instant lockedUntil) {
        return new User(
            UUID.randomUUID(), UUID.randomUUID(),
            "U", new Email("u@delta.com"), new HashedPassword("HASH"),
            status, false, null, null,
            failedAttempts, lockedUntil, now, now
        );
    }

    private User userWith(int failedAttempts) {
        return userWith(UserStatus.ACTIVE, failedAttempts, null);
    }

    private PasswordHasher hasher(boolean matches) {
        PasswordHasher hasher = mock(PasswordHasher.class);
        when(hasher.matches(any(), any())).thenReturn(matches);
        return hasher;
    }

    @Test
    void wrongPasswordIncrementsTheCounter() {
        User user = userWith(0);
        assertThrows(InvalidCredentialsException.class, () -> user.authenticate("bad", hasher(false), now, policy));
        assertEquals(1, user.snapshot().failedAttempts());
        assertEquals(UserStatus.ACTIVE, user.snapshot().status());
    }

    @Test
    void fifthFailureAppliesTheFirstCooldown() {
        User user = userWith(4);
        assertThrows(InvalidCredentialsException.class, () -> user.authenticate("bad", hasher(false), now, policy));
        UserSnapshot s = user.snapshot();
        assertEquals(5, s.failedAttempts());
        assertEquals(UserStatus.LOCKED, s.status());
        assertEquals(now.plus(Duration.ofMinutes(5)), s.lockedUntil());
    }

    @Test
    void correctPasswordDuringActiveCooldownThrowsUserLockedAndDoesNotIncrement() {
        Instant until = now.plus(Duration.ofMinutes(5));
        User user = userWith(UserStatus.LOCKED, 5, until);
        UserLockedException ex = assertThrows(UserLockedException.class,
            () -> user.authenticate("good", hasher(true), now, policy));
        assertEquals(until, ex.getLockedUntil());
        assertEquals(5, user.snapshot().failedAttempts());
    }

    @Test
    void expiredCooldownReleasesAndTheNextWrongPasswordKeepsAccumulating() {
        Instant past = now.minus(Duration.ofSeconds(1));
        User user = userWith(UserStatus.LOCKED, 5, past);
        assertThrows(InvalidCredentialsException.class, () -> user.authenticate("bad", hasher(false), now, policy));
        UserSnapshot s = user.snapshot();
        assertEquals(6, s.failedAttempts());
        assertEquals(UserStatus.ACTIVE, s.status());
        assertNull(s.lockedUntil());
    }

    @Test
    void eighthFailureAppliesTheSecondCooldown() {
        User user = userWith(7);
        assertThrows(InvalidCredentialsException.class, () -> user.authenticate("bad", hasher(false), now, policy));
        assertEquals(now.plus(Duration.ofMinutes(15)), user.snapshot().lockedUntil());
    }

    @Test
    void tenthFailureLocksPermanently() {
        User user = userWith(9);
        assertThrows(InvalidCredentialsException.class, () -> user.authenticate("bad", hasher(false), now, policy));
        UserSnapshot s = user.snapshot();
        assertEquals(10, s.failedAttempts());
        assertEquals(UserStatus.LOCKED, s.status());
        assertNull(s.lockedUntil());
    }

    @Test
    void correctPasswordWhilePermanentlyLockedThrowsUserLockedWithNull() {
        User user = userWith(UserStatus.LOCKED, 20, null);
        UserLockedException ex = assertThrows(UserLockedException.class,
            () -> user.authenticate("good", hasher(true), now, policy));
        assertNull(ex.getLockedUntil());
    }

    @Test
    void successBeforeTheLimitResetsTheCounter() {
        User user = userWith(3);
        user.authenticate("good", hasher(true), now, policy);
        UserSnapshot s = user.snapshot();
        assertEquals(0, s.failedAttempts());
        assertEquals(UserStatus.ACTIVE, s.status());
        assertNotNull(s.lastLoginAt());
    }

    @Test
    void successAfterAnExpiredCooldownReleasesAndResets() {
        User user = userWith(UserStatus.LOCKED, 5, now.minus(Duration.ofMinutes(1)));
        user.authenticate("good", hasher(true), now, policy);
        UserSnapshot s = user.snapshot();
        assertEquals(0, s.failedAttempts());
        assertEquals(UserStatus.ACTIVE, s.status());
        assertNull(s.lockedUntil());
    }

    @Test
    void disabledPolicyIncrementsButNeverLocks() {
        User user = userWith(4);
        LockoutPolicy disabled = new LockoutPolicy(false, policy.attemptsPerLevel(), policy.durations());
        assertThrows(InvalidCredentialsException.class, () -> user.authenticate("bad", hasher(false), now, disabled));
        UserSnapshot s = user.snapshot();
        assertEquals(5, s.failedAttempts());
        assertEquals(UserStatus.ACTIVE, s.status());
        assertNull(s.lockedUntil());
    }
}
