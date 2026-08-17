package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class AddressTests {

    @Test
    fun `must accept a valid address`() {
        assertDoesNotThrow {
            Address(cep = "01310100", street = "Av Paulista", city = "São Paulo", state = Uf("SP"))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["123", "0131010", "013101000", "abcdefgh"])
    fun `must reject invalid cep`(invalidCep: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Address(cep = invalidCep, street = "Rua X", city = "SP", state = Uf("SP"))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `must reject blank street`(blankStreet: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Address(cep = "01310100", street = blankStreet, city = "SP", state = Uf("SP"))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `must reject blank city`(blankCity: String) {
        assertThrows(IllegalArgumentException::class.java) {
            Address(cep = "01310100", street = "Av Paulista", city = blankCity, state = Uf("SP"))
        }
    }

    @Test
    fun `must accept address with null optional fields`() {
        assertDoesNotThrow {
            Address(
                cep = "01310100", street = "Av Paulista", city = "São Paulo", state = Uf("SP"),
                number = null, complement = null, neighborhood = null,
            )
        }
    }

    @Test
    fun `must default country to BR when not provided`() {
        val address = Address(cep = "01310100", street = "Av Paulista", city = "São Paulo", state = Uf("SP"))
        assertEquals("BR", address.country)
    }

    @Test
    fun `must keep all provided fields`() {
        val address = Address(
            cep = "01310100", street = "Av Paulista", city = "São Paulo", state = Uf("SP"),
            number = "1000", complement = "10º andar", neighborhood = "Bela Vista", country = "BR",
        )
        assertEquals("01310100", address.cep)
        assertEquals("Bela Vista", address.neighborhood)
    }
}