package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class EmailTests {
    @ParameterizedTest
    @ValueSource(strings = ["user@delta.com", "user.name@delta.com.br"])
    fun `must accept a valid email`(valid: String) {
        assertDoesNotThrow { Email(valid) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["user_delta.com", "user@", "user@delta", "@delta.com", "", " "])
    fun `must reject email when email is invalid`(invalidEmail: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Email(invalidEmail)
        }
    }

    @Test
    fun `must normalize to lowercase`() {
        assertEquals("admin@delta.com", Email("ADMIN@Delta.Com").value)
    }
}