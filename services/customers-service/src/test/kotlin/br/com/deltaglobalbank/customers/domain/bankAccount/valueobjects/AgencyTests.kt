package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AgencyTests {

    @ParameterizedTest
    @ValueSource(strings = ["1234", "0001", "9999"])
    fun `must accept a valid agency`(valid: String) {
        assertDoesNotThrow { Agency(valid) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["123", "12345", "abc", "12a", ""])
    fun `must reject an invalid agency`(invalid: String) {
        assertThrows(IllegalArgumentException::class.java) { Agency(invalid) }
    }
}