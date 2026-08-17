package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class CpfTests {

    @ParameterizedTest
    @ValueSource(strings = ["11144477735", "111.444.777-35"])
    fun `must accept a valid cpf`(validCpf: String) {
        assertDoesNotThrow { Cpf(validCpf) }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "11144477700",
        "11144477734",
        "11111111111",
        "123",
        "",
    ])
    fun `must reject an invalid cpf`(invalidCpf: String) {
        assertThrows(IllegalArgumentException::class.java) { Cpf(invalidCpf) }
    }

    @ParameterizedTest
    @CsvSource("111.444.777-35, 11144477735", "111444777-35, 11144477735")
    fun `normalizes cpf`(input: String, expected: String) {
        assertEquals(expected, Cpf(input).value)
    }

    @Test
    fun `normalizes a masked cpf to digits only`() {
        val cpf = Cpf("111.444.777-35")
        assertEquals("11144477735", cpf.value)
    }

    @Test
    fun `rejects cpf with invalid check digit`() {
        assertFailsWith<IllegalArgumentException> { Cpf("11144477700") }
    }

    @Test
    fun `rejects cpf with valid first but invalid second check digit`() {
        assertFailsWith<IllegalArgumentException> { Cpf("11144477734") }
    }

    @Test
    fun `rejects cpf with all repeated digits`() {
        assertFailsWith<IllegalArgumentException> { Cpf("11111111111") }
    }

    @Test
    fun `rejects cpf with wrong length`() {
        assertFailsWith<IllegalArgumentException> { Cpf("123") }
    }
}