package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

public record HeroSegurosProposalCivil(
    Integer id,
    String name
) {
    public HeroSegurosProposalCivil() {
        this(null, null);
    }
}
