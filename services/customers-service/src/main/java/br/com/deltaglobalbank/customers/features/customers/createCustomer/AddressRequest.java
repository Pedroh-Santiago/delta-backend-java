package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
    @NotBlank String cep,
    @NotBlank String street,
    String number,
    String complement,
    String neighborhood,
    @NotBlank String city,
    @NotBlank String state,
    String country
) {
    public AddressRequest {
        if (country == null) {
            country = "BR";
        }
    }
}
