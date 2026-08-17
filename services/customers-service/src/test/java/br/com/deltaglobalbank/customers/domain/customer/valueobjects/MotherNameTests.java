package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MotherNameTests {

    @Test
    void mustAcceptAValidMotherName() {
        assertDoesNotThrow(() -> new MotherName("Ana Silva"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void mustRejectBlankMotherName(String blank) {
        assertThrows(IllegalArgumentException.class, () -> new MotherName(blank));
    }

    @Test
    void mustRejectMotherNameExceedingMaxLength() {
        assertThrows(IllegalArgumentException.class, () -> new MotherName("a".repeat(256)));
    }

    @Test
    void mustTrimSurroundingSpaces() {
        assertEquals("Ana Silva", new MotherName("  Ana Silva  ").value());
    }
}
