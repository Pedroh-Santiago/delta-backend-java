package br.com.deltaglobalbank.delta_secure.features.heroseguros;

public final class TermoAdesaoNaoEncontrado extends HeroSegurosException {

    public TermoAdesaoNaoEncontrado(String ticket) {
        super("termo de adesão não encontrado para o ticket " + ticket);
    }
}
