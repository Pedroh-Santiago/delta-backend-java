package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

public record AddressResponse(
    String cep,
    String street,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state,
    String country
) {
}
