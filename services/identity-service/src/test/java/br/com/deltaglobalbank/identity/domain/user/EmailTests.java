package br.com.deltaglobalbank.identity.domain.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTests {

    @ParameterizedTest
    @ValueSource(strings = {"user@delta.com", "user.name@delta.com", "user+name@delta.com.br", "u123@delta.global.com"})
    void mustAcceptAValidEmail(String validEmail) {
        assertDoesNotThrow(() -> new Email(validEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin_delta.com", "admin@", "admin@delta", "admin@@delta.com", "@delta.com"})
    void mustRejectEmailWhenEmailIsInvalid(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void mustRejectBlankEmail(String blankEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(blankEmail));
    }

    @Test
    void mustRejectEmailExceedingMaxLength() {
        String tooLongEmail = "a".repeat(250) + "@delta.com";
        assertThrows(IllegalArgumentException.class, () -> new Email(tooLongEmail));
    }

    @Test
    void mustNormalizeEmailToLowercase() {
        Email emailUpperCase = new Email("ADMIN@DELTA.COM");
        assertEquals("admin@delta.com", emailUpperCase.value());
    }

    @Test
    void mustTreatEmailsDifferingOnlyByCaseAsEqual() {
        assertEquals(new Email("admin@delta.com"), new Email("Admin@Delta.Com"));
    }
}
