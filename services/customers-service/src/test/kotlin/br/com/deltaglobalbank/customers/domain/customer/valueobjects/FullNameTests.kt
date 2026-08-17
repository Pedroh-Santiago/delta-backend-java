package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class FullNameTests {
    @Test
    fun `must accept a valid name`() {
        assertDoesNotThrow { FullName("Maria Silva") }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `must reject blank name`(blank: String) {
        assertThrows(IllegalArgumentException::class.java) { FullName(blank) }
    }

    @Test
    fun `must reject name exceeding max length`() {
        assertThrows(IllegalArgumentException::class.java) { FullName("a".repeat(256)) }
    }

    @Test
    fun `must trim surrounding spaces`() {
        assertEquals("Maria Silva", FullName("  Maria Silva  ").value)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Maria", "ab", "João"])
    fun `must reject single word or short name`(invalid: String) {
        assertThrows(IllegalArgumentException::class.java) { FullName(invalid) }
    }
}