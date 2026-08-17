package br.com.deltaglobalbank.customers.features.customers.getCustomer;

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
