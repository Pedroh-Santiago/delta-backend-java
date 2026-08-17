package br.com.deltaglobalbank.customers.domain.shared.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class UfTests {

    @ParameterizedTest
    @ValueSource(strings = ["SP", "RJ", "MG"])
    fun `must accept a valid uf`(validUf: String) {
        assertDoesNotThrow { Uf(validUf) }
    }

    @Test
    fun `must normalize lowercase uf to uppercase`() {
        assertEquals("SP", Uf("sp").value)
    }

    @ParameterizedTest
    @ValueSource(strings = ["SPX", "S1", "1A", "", " "])
    fun `must reject invalid uf`(invalidUf: String) {
        assertThrows(IllegalArgumentException::class.java) { Uf(invalidUf) }
    }
}