package br.com.deltaglobalbank.customers.domain.bankAccount.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BankCodeTests {

    @ParameterizedTest
    @ValueSource(strings = {"001", "237", "260"})
    void mustAcceptAValidBankCode(String valid) {
        assertDoesNotThrow(() -> new BankCode(valid));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "1234", "abc", "", "23a"})
    void mustRejectAnInvalidBankCode(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new BankCode(invalid));
    }
}
