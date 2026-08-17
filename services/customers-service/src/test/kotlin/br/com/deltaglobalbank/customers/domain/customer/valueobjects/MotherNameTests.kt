package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class MotherNameTests {

    @Test
    fun `must accept a valid mother name`() {
        assertDoesNotThrow { MotherName("Ana Silva") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `must reject blank mother name`(blank: String) {
        assertThrows(IllegalArgumentException::class.java) { MotherName(blank) }
    }

    @Test
    fun `must reject mother name exceeding max length`() {
        assertThrows(IllegalArgumentException::class.java) { MotherName("a".repeat(256)) }
    }

    @Test
    fun `must trim surrounding spaces`() {
        assertEquals("Ana Silva", MotherName("  Ana Silva  ").value)
    }
}