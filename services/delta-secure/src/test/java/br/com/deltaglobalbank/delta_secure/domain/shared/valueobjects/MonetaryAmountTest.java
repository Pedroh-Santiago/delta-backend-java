package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MonetaryAmountTest {

    @Test
    void valorInteiroNaoLevaCasasDecimais() {
        assertEquals("5000", MonetaryAmount.toSWorksString(5000.0));
        assertEquals("0", MonetaryAmount.toSWorksString(0.0));
    }

    @Test
    void valorComCentavosLevaDuasCasas() {
        assertEquals("1234.50", MonetaryAmount.toSWorksString(1234.5));
        assertEquals("99.99", MonetaryAmount.toSWorksString(99.99));
    }
}
