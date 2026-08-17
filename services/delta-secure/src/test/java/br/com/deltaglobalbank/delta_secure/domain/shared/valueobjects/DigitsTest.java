package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DigitsTest {

    @Test
    void tiraAMascaraDoCelular() {
        assertEquals("11933989960", Digits.onlyDigits("(11) 93398-9960"));
        assertEquals("11933989960", Digits.onlyDigits("11933989960"));
    }
}
