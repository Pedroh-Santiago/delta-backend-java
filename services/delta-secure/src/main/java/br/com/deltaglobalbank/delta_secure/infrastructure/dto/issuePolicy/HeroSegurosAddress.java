package br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy;

public record HeroSegurosAddress(
    String cep,
    String address,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state
) {
}
