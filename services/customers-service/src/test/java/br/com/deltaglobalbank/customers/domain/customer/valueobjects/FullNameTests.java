package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FullNameTests {

    @Test
    void mustAcceptAValidName() {
        assertDoesNotThrow(() -> new FullName("Maria Silva"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void mustRejectBlankName(String blank) {
        assertThrows(IllegalArgumentException.class, () -> new FullName(blank));
    }

    @Test
    void mustRejectNameExceedingMaxLength() {
        assertThrows(IllegalArgumentException.class, () -> new FullName("a".repeat(256)));
    }

    @Test
    void mustTrimSurroundingSpaces() {
        assertEquals("Maria Silva", new FullName("  Maria Silva  ").value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Maria", "ab", "João"})
    void mustRejectSingleWordOrShortName(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new FullName(invalid));
    }
}
