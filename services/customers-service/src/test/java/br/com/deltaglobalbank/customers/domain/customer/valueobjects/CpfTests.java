package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTests {

    @ParameterizedTest
    @ValueSource(strings = {"11144477735", "111.444.777-35"})
    void mustAcceptAValidCpf(String validCpf) {
        assertDoesNotThrow(() -> new Cpf(validCpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "11144477700",
        "11144477734",
        "11111111111",
        "123",
        "",
    })
    void mustRejectAnInvalidCpf(String invalidCpf) {
        assertThrows(IllegalArgumentException.class, () -> new Cpf(invalidCpf));
    }

    @ParameterizedTest
    @CsvSource({"111.444.777-35, 11144477735", "111444777-35, 11144477735"})
    void normalizesCpf(String input, String expected) {
        assertEquals(expected, new Cpf(input).value());
    }

    @Test
    void normalizesAMaskedCpfToDigitsOnly() {
        Cpf cpf = new Cpf("111.444.777-35");
        assertEquals("11144477735", cpf.value());
    }

    @Test
    void rejectsCpfWithInvalidCheckDigit() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("11144477700"));
    }

    @Test
    void rejectsCpfWithValidFirstButInvalidSecondCheckDigit() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("11144477734"));
    }

    @Test
    void rejectsCpfWithAllRepeatedDigits() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("11111111111"));
    }

    @Test
    void rejectsCpfWithWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("123"));
    }
}
