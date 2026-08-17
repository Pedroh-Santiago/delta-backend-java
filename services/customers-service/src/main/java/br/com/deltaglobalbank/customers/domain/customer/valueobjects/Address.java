package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import java.util.Objects;

import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf;

public record Address(
    String cep,
    String street,
    String city,
    Uf state,
    String country,
    String number,
    String complement,
    String neighborhood
) {
    public Address {
        Objects.requireNonNull(cep, "address_cep_required");
        Objects.requireNonNull(street, "address_street_required");
        Objects.requireNonNull(city, "address_city_required");
        Objects.requireNonNull(state, "address_state_required");
        if (!cep.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("address_cep_invalid");
        }
        if (street.isBlank()) {
            throw new IllegalArgumentException("address_street_blank");
        }
        if (city.isBlank()) {
            throw new IllegalArgumentException("address_city_blank");
        }
    }

    public Address(String cep, String street, String city, Uf state) {
        this(cep, street, city, state, "BR", null, null, null);
    }
}
