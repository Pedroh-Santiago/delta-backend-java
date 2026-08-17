package br.com.deltaglobalbank.identity.domain.user

import br.com.deltaglobalbank.identity.domain.shared.PasswordHasher
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserTests {

    @Nested
    inner class Authentication {
        @Test
        fun `must authenticate successfully and reset failed attempts on valid credentials`() {
            val user = User(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                fullName = "New User",
                email = Email("admin@delta.com"),
                passwordHash = HashedPassword("Hashed Password"),
                status = UserStatus.ACTIVE,
                mustChangePassword = false,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 3,
                lockedUntil = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val hasher = mockk<PasswordHasher>()
            every { hasher.matches("Password", HashedPassword("Hashed Password")) } returns true

            user.authenticate("Password", hasher)

            val snap = user.snapshot()
            assertAll(
                { assertEquals(0, snap.failedAttempts) },
                { assertNull(snap.lockedUntil) },
                { assertNotNull(snap.lastLoginAt) }

            )
        }

        @Test
        fun `must throw and increment failed attempts when password does not match`() {
            val user = User(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                fullName = "New User",
                email = Email("admin@delta.com"),
                passwordHash = HashedPassword("Hashed Password"),
                status = UserStatus.ACTIVE,
                mustChangePassword = false,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 2,
                lockedUntil = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val hasher = mockk<PasswordHasher>()
            every { hasher.matches("wrong-password", HashedPassword("Hashed Password")) } returns false

            assertThrows(InvalidCredentialsException::class.java) {
                user.authenticate("wrong-password", hasher)
            }

            assertEquals(3, user.snapshot().failedAttempts)
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus::class, names = ["SUSPENDED", "LOCKED"])
        fun `must throw when user status is not active`(status: UserStatus) {
            val user = User(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                fullName = "New User",
                email = Email("admin@delta.com"),
                passwordHash = HashedPassword("Hashed Password"),
                status = status,
                mustChangePassword = false,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 0,
                lockedUntil = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val hasher = mockk<PasswordHasher>()
            every { hasher.matches("Password", HashedPassword("Hashed Password")) } returns true

            assertThrows(UserNotActiveException::class.java) {
                user.authenticate("Password", hasher)
            }
        }
    }

    @Nested
    inner class ChangePassword {
        @Test
        fun `must update password hash and clear must-change flag on successful change`(){
            val user = User(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                fullName = "New User",
                email = Email("admin@delta.com"),
                passwordHash = HashedPassword("Hashed Old Password"),
                status = UserStatus.ACTIVE,
                mustChangePassword = true,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 0,
                lockedUntil = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val currentPassword = Password("OldPassword1!")
            val newPassword = Password("NewPassword1!")

            val hasher = mockk<PasswordHasher>()
            every { hasher.matches(currentPassword.value, HashedPassword("Hashed Old Password")) } returns true
            every { hasher.hash(newPassword) } returns HashedPassword("New Hashed Password")

            user.changePassword(currentPassword, newPassword, hasher)

            val snap = user.snapshot()
            assertAll(
                { assertEquals(HashedPassword("New Hashed Password"), snap.passwordHash) },
                {assertEquals(false, snap.mustChangePassword)},
                { kotlin.test.assertNotNull(snap.passwordChangedAt)}
            )
        }

        @Test
        fun `must throw when current password does not match`(){
            val user = User(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                fullName = "New User",
                email = Email("admin@delta.com"),
                passwordHash = HashedPassword("Old Hashed Password"),
                status = UserStatus.ACTIVE,
                mustChangePassword = true,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 0,
                lockedUntil = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val currentPassword = Password("OldPassword1!")
            val newPassword = Password("NewPassword1!")

            val hasher = mockk<PasswordHasher>()
            every { hasher.matches(currentPassword.value, HashedPassword("Old Hashed Password")) } returns false

            assertThrows (CurrentPasswordIncorrectException::class.java) {
                user.changePassword(currentPassword, newPassword, hasher)
            }

            assertEquals(HashedPassword("Old Hashed Password"), user.snapshot().passwordHash)
        }

        @Test
        fun `must throw when new password equals current password`(){
            val user = User(
                id = UUID.randomUUID(),
                tenantId = UUID.randomUUID(),
                fullName = "New User",
                email = Email("admin@delta.com"),
                passwordHash = HashedPassword("Same Hashed Password"),
                status = UserStatus.ACTIVE,
                mustChangePassword = true,
                passwordChangedAt = null,
                lastLoginAt = null,
                failedAttempts = 0,
                lockedUntil = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val samePassword = Password("Password1!")

            val hasher = mockk<PasswordHasher>()
            every { hasher.matches(samePassword.value, HashedPassword("Same Hashed Password")) } returns true

            assertThrows(NewPasswordSameAsCurrentException::class.java) {
                user.changePassword(samePassword, samePassword, hasher)
            }

            assertEquals(HashedPassword("Same Hashed Password"), user.snapshot().passwordHash)
        }
    }

}