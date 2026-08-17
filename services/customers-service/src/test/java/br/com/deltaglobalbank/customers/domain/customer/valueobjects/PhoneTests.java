package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "+5511999998888",
        "+12345678",
        "+123456789012345",
    })
    void mustAcceptAValidE164Phone(String validPhone) {
        assertDoesNotThrow(() -> new Phone(validPhone));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "5511999998888",
        "+55abc999888",
        "+123",
        "+1234567890123456",
        "",
        "+55 11 99999",
    })
    void mustRejectAnInvalidPhone(String invalidPhone) {
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    void mustKeepTheValueAsProvided() {
        assertEquals("+5511999998888", new Phone("+5511999998888").value());
    }
}
