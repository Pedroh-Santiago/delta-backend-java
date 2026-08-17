package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BankCodeTests {
    @ParameterizedTest
    @ValueSource(strings = ["001", "237", "260"])
    fun `must accept a valid bank code`(valid: String) {
        assertDoesNotThrow { BankCode(valid) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["12", "1234", "abc", "", "23a"])
    fun `must reject an invalid bank code`(invalid: String) {
        assertThrows(IllegalArgumentException::class.java) { BankCode(invalid) }
    }
}