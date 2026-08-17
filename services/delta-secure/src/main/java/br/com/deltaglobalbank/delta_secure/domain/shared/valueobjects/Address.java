package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

public record Address(
    String cep,
    String address,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state
) {
}
