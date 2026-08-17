package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AgencyTests {

    @ParameterizedTest
    @ValueSource(strings = {"1234", "0001", "9999"})
    void mustAcceptAValidAgency(String valid) {
        assertDoesNotThrow(() -> new Agency(valid));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345", "abc", "12a", ""})
    void mustRejectAnInvalidAgency(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Agency(invalid));
    }
}
