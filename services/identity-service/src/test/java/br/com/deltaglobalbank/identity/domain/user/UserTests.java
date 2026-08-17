package br.com.deltaglobalbank.identity.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UserTests {

    private final LockoutPolicy policy = new LockoutPolicy(
        true,
        List.of(5, 3, 2),
        List.of(Duration.ofMinutes(5), Duration.ofMinutes(15))
    );

    @Nested
    class Authentication {

        @Test
        void mustAuthenticateSuccessfullyAndResetFailedAttemptsOnValidCredentials() {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Hashed Password"),
                UserStatus.ACTIVE, false, null, null, 3, null,
                Instant.now(), Instant.now()
            );

            PasswordHasher hasher = mock(PasswordHasher.class);
            when(hasher.matches("Password", new HashedPassword("Hashed Password"))).thenReturn(true);

            user.authenticate("Password", hasher, Instant.now(), policy);

            UserSnapshot snap = user.snapshot();
            Assertions.assertAll(
                () -> assertEquals(0, snap.failedAttempts()),
                () -> assertNull(snap.lockedUntil()),
                () -> assertNotNull(snap.lastLoginAt())
            );
        }

        @Test
        void mustThrowAndIncrementFailedAttemptsWhenPasswordDoesNotMatch() {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Hashed Password"),
                UserStatus.ACTIVE, false, null, null, 2, null,
                Instant.now(), Instant.now()
            );

            PasswordHasher hasher = mock(PasswordHasher.class);
            when(hasher.matches("wrong-password", new HashedPassword("Hashed Password"))).thenReturn(false);

            assertThrows(InvalidCredentialsException.class,
                () -> user.authenticate("wrong-password", hasher, Instant.now(), policy));

            assertEquals(3, user.snapshot().failedAttempts());
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"SUSPENDED"})
        void mustThrowWhenUserStatusIsNotActive(UserStatus status) {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Hashed Password"),
                status, false, null, null, 0, null,
                Instant.now(), Instant.now()
            );

            PasswordHasher hasher = mock(PasswordHasher.class);
            lenient().when(hasher.matches("Password", new HashedPassword("Hashed Password"))).thenReturn(true);

            assertThrows(UserNotActiveException.class,
                () -> user.authenticate("Password", hasher, Instant.now(), policy));
        }
    }

    @Nested
    class ChangePassword {

        @Test
        void mustUpdatePasswordHashAndClearMustChangeFlagOnSuccessfulChange() {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Hashed Old Password"),
                UserStatus.ACTIVE, true, null, null, 0, null,
                Instant.now(), Instant.now()
            );

            Password currentPassword = new Password("OldPassword1!");
            Password newPassword = new Password("NewPassword1!");

            PasswordHasher hasher = mock(PasswordHasher.class);
            when(hasher.matches(currentPassword.value(), new HashedPassword("Hashed Old Password"))).thenReturn(true);
            when(hasher.hash(newPassword)).thenReturn(new HashedPassword("New Hashed Password"));

            user.changePassword(currentPassword, newPassword, hasher);

            UserSnapshot snap = user.snapshot();
            Assertions.assertAll(
                () -> assertEquals(new HashedPassword("New Hashed Password"), snap.passwordHash()),
                () -> assertEquals(false, snap.mustChangePassword()),
                () -> assertNotNull(snap.passwordChangedAt())
            );
        }

        @Test
        void mustThrowWhenCurrentPasswordDoesNotMatch() {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Old Hashed Password"),
                UserStatus.ACTIVE, true, null, null, 0, null,
                Instant.now(), Instant.now()
            );

            Password currentPassword = new Password("OldPassword1!");
            Password newPassword = new Password("NewPassword1!");

            PasswordHasher hasher = mock(PasswordHasher.class);
            when(hasher.matches(currentPassword.value(), new HashedPassword("Old Hashed Password"))).thenReturn(false);

            assertThrows(CurrentPasswordIncorrectException.class,
                () -> user.changePassword(currentPassword, newPassword, hasher));

            assertEquals(new HashedPassword("Old Hashed Password"), user.snapshot().passwordHash());
        }

        @Test
        void mustThrowWhenNewPasswordEqualsCurrentPassword() {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Same Hashed Password"),
                UserStatus.ACTIVE, true, null, null, 0, null,
                Instant.now(), Instant.now()
            );

            Password samePassword = new Password("Password1!");

            PasswordHasher hasher = mock(PasswordHasher.class);
            when(hasher.matches(samePassword.value(), new HashedPassword("Same Hashed Password"))).thenReturn(true);

            assertThrows(NewPasswordSameAsCurrentException.class,
                () -> user.changePassword(samePassword, samePassword, hasher));

            assertEquals(new HashedPassword("Same Hashed Password"), user.snapshot().passwordHash());
        }

        @Test
        void mustThrowUserLockedWhenAccountIsPermanentlyLocked() {
            User user = new User(
                UUID.randomUUID(), UUID.randomUUID(), "New User",
                new Email("admin@delta.com"), new HashedPassword("Hashed Password"),
                UserStatus.LOCKED, false, null, null, 20, null,
                Instant.now(), Instant.now()
            );

            PasswordHasher hasher = mock(PasswordHasher.class);

            assertThrows(UserLockedException.class,
                () -> user.authenticate("Password", hasher, Instant.now(), policy));
        }
    }
}
