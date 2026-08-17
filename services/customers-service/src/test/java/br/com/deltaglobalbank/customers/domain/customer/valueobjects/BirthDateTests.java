package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class BirthDateTests {

    @Test
    void mustAcceptAnAdult() {
        assertDoesNotThrow(() -> new BirthDate(LocalDate.of(1990, 5, 20)));
    }

    @Test
    void mustAcceptSomeoneWhoJustTurned18() {
        assertDoesNotThrow(() -> new BirthDate(LocalDate.now().minusYears(18)));
    }

    @Test
    void mustRejectSomeoneUnder18() {
        assertThrows(IllegalArgumentException.class,
            () -> new BirthDate(LocalDate.now().minusYears(18).plusDays(1)));
    }

    @Test
    void mustRejectAFutureDate() {
        assertThrows(IllegalArgumentException.class, () -> new BirthDate(LocalDate.now().plusDays(1)));
    }
}
