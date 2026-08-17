package br.com.deltaglobalbank.identity.domain.user

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EmailTests {

    @ParameterizedTest
    @ValueSource(strings = ["user@delta.com", "user.name@delta.com", "user+name@delta.com.br", "u123@delta.global.com"])
    fun `must accept a valid email`(validEmail: String) {
        assertDoesNotThrow { Email(validEmail) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["admin_delta.com", "admin@", "admin@delta", "admin@@delta.com", "@delta.com"])
    fun `must reject email when email is invalid`(invalidEmail: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Email(invalidEmail)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `must reject blank email`(blankEmail: String) {
        assertThrows(IllegalArgumentException::class.java) { Email(blankEmail) }
    }

    @Test
    fun `must reject email exceeding max length`() {
        val tooLongEmail = "a".repeat(250) + "@delta.com"
        assertThrows(IllegalArgumentException::class.java) { Email(tooLongEmail) }
    }

    @Test
    fun `must normalize email to lowercase`() {
        val emailUpperCase = Email ("ADMIN@DELTA.COM")
        assertEquals("admin@delta.com", emailUpperCase.value)
    }

    @Test
    fun `must treat emails differing only by case as equal`() {
        assertEquals(Email("admin@delta.com"), Email("Admin@Delta.Com"))
    }

}