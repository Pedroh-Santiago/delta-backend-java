package br.com.deltaglobalbank.customers.domain.shared.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UfTests {

    @ParameterizedTest
    @ValueSource(strings = {"SP", "RJ", "MG"})
    void mustAcceptAValidUf(String validUf) {
        assertDoesNotThrow(() -> new Uf(validUf));
    }

    @Test
    void mustNormalizeLowercaseUfToUppercase() {
        assertEquals("SP", new Uf("sp").value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SPX", "S1", "1A", "", " "})
    void mustRejectInvalidUf(String invalidUf) {
        assertThrows(IllegalArgumentException.class, () -> new Uf(invalidUf));
    }
}
