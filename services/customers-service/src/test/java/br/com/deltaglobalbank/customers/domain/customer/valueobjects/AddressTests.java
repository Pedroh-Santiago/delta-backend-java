package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddressTests {

    @Test
    void mustAcceptAValidAddress() {
        assertDoesNotThrow(() -> new Address("01310100", "Av Paulista", "São Paulo", new Uf("SP")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "0131010", "013101000", "abcdefgh"})
    void mustRejectInvalidCep(String invalidCep) {
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidCep, "Rua X", "SP", new Uf("SP")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void mustRejectBlankStreet(String blankStreet) {
        assertThrows(IllegalArgumentException.class, () -> new Address("01310100", blankStreet, "SP", new Uf("SP")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void mustRejectBlankCity(String blankCity) {
        assertThrows(IllegalArgumentException.class, () -> new Address("01310100", "Av Paulista", blankCity, new Uf("SP")));
    }

    @Test
    void mustAcceptAddressWithNullOptionalFields() {
        assertDoesNotThrow(() -> new Address(
            "01310100", "Av Paulista", "São Paulo", new Uf("SP"), "BR", null, null, null
        ));
    }

    @Test
    void mustDefaultCountryToBrWhenNotProvided() {
        Address address = new Address("01310100", "Av Paulista", "São Paulo", new Uf("SP"));
        assertEquals("BR", address.country());
    }

    @Test
    void mustKeepAllProvidedFields() {
        Address address = new Address(
            "01310100", "Av Paulista", "São Paulo", new Uf("SP"),
            "BR", "1000", "10º andar", "Bela Vista"
        );
        assertEquals("01310100", address.cep());
        assertEquals("Bela Vista", address.neighborhood());
    }
}
