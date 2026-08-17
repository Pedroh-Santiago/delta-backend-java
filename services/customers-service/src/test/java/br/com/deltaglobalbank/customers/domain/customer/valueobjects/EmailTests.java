package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTests {

    @ParameterizedTest
    @ValueSource(strings = {"user@delta.com", "user.name@delta.com.br"})
    void mustAcceptAValidEmail(String valid) {
        assertDoesNotThrow(() -> new Email(valid));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user_delta.com", "user@", "user@delta", "@delta.com", "", " "})
    void mustRejectEmailWhenEmailIsInvalid(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    void mustNormalizeToLowercase() {
        assertEquals("admin@delta.com", new Email("ADMIN@Delta.Com").value());
    }
}
