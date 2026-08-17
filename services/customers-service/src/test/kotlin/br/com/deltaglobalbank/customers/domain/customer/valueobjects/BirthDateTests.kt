package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import java.time.LocalDate
import org.junit.jupiter.api.Test

class BirthDateTests {

    @Test
    fun `must accept an adult`() {
        assertDoesNotThrow { BirthDate(LocalDate.of(1990, 5, 20)) }
    }

    @Test
    fun `must accept someone who just turned 18`() {
        assertDoesNotThrow { BirthDate(LocalDate.now().minusYears(18)) }
    }

    @Test
    fun `must reject someone under 18`() {
        assertThrows(IllegalArgumentException::class.java) {
            BirthDate(LocalDate.now().minusYears(18).plusDays(1))
        }
    }

    @Test
    fun `must reject a future date`() {
        assertThrows(IllegalArgumentException::class.java) { BirthDate(LocalDate.now().plusDays(1)) }
    }
}