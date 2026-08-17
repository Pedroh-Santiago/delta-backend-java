package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

public record HeroSegurosProposalAddress(
    Integer id,
    String cep,
    String address,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state
) {
    public HeroSegurosProposalAddress() {
        this(null, null, null, null, null, null, null, null);
    }
}
