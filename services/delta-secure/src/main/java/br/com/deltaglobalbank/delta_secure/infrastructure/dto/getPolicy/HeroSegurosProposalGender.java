package br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy;

public record HeroSegurosProposalGender(
    Integer id,
    String name,
    String code
) {
    public HeroSegurosProposalGender() {
        this(null, null, null);
    }
}
