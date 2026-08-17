package br.com.deltaglobalbank.domain.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf;
import org.junit.jupiter.api.Test;

class CpfTest {

    @Test
    void deveCriarCpfComSucessoQuandoOValorRespeitarOAlgoritmo() {
        String cpfValido = "11144477735";

        Cpf cpf = new Cpf(cpfValido);

        assertEquals(cpfValido, cpf.value());
    }

    @Test
    void deveLancarExcecaoQuandoCpfForVazio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Cpf(""));
        assertEquals("Cpf_notBlank", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCpfTiverMenosDe11Digitos() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Cpf("123456789"));
        assertEquals("Cpf_length is not 11", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCpfTiverMaisDe11Digitos() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Cpf("123456789012"));
        assertEquals("Cpf_length is not 11", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCpfContiverLetras() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Cpf("1234567890a"));
        assertEquals("Cpf_onlyDigits", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCpfTiverTodosOsDigitosIguais() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Cpf("11111111111"));
        assertEquals("Cpf_invalid", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCpfTiverDigitosVerificadoresErrados() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Cpf("51336499000"));
        assertEquals("Cpf_invalid", exception.getMessage());
    }
}
