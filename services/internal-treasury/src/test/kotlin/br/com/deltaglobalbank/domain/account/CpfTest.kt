package br.com.deltaglobalbank.domain.account

import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CpfTest {

    @Test
    fun `deve criar cpf com sucesso quando o valor respeitar o algoritmo`() {
        val cpfValido = "11144477735"

        val cpf = Cpf(cpfValido)

        assertEquals(cpfValido, cpf.value)
    }

    @Test
    fun `deve lancar excecao quando cpf for vazio`() {
        val exception = assertThrows<IllegalArgumentException> {
            Cpf("")
        }
        assertEquals("Cpf_notBlank", exception.message)
    }

    @Test
    fun `deve lancar excecao quando cpf tiver menos de 11 digitos`() {
        val exception = assertThrows<IllegalArgumentException> {
            Cpf("123456789")
        }
        assertEquals("Cpf_length is not 11", exception.message)
    }

    @Test
    fun `deve lancar excecao quando cpf tiver mais de 11 digitos`() {
        val exception = assertThrows<IllegalArgumentException> {
            Cpf("123456789012")
        }
        assertEquals("Cpf_length is not 11", exception.message)
    }

    @Test
    fun `deve lancar excecao quando cpf contiver letras`() {
        val exception = assertThrows<IllegalArgumentException> {
            Cpf("1234567890a")
        }
        assertEquals("Cpf_onlyDigits", exception.message)
    }

    @Test
    fun `deve lancar excecao quando cpf tiver todos os digitos iguais`() {
        val exception = assertThrows<IllegalArgumentException> {
            Cpf("11111111111")
        }
        assertEquals("Cpf_invalid", exception.message)
    }

    @Test
    fun `deve lancar excecao quando cpf tiver digitos verificadores errados`() {
        val exception = assertThrows<IllegalArgumentException> {
            Cpf("51336499000")
        }
        assertEquals("Cpf_invalid", exception.message)
    }
}