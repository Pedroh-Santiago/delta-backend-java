package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class PhoneTests {

    @ParameterizedTest
    @ValueSource(strings = [
        "+5511999998888",
        "+12345678",
        "+123456789012345",
    ])
    fun `must accept a valid e164 phone`(validPhone: String) {
        assertDoesNotThrow { Phone(validPhone) }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "5511999998888",
        "+55abc999888",
        "+123",
        "+1234567890123456",
        "",
        "+55 11 99999",
    ])
    fun `must reject an invalid phone`(invalidPhone: String) {
        assertThrows(IllegalArgumentException::class.java) { Phone(invalidPhone) }
    }

    @Test
    fun `must keep the value as provided`() {
        assertEquals("+5511999998888", Phone("+5511999998888").value)
    }
}