package br.com.deltaglobalbank.delta_secure.features.heroseguros;

public final class HeroSegurosInvalidResponse extends HeroSegurosException {

    public HeroSegurosInvalidResponse(Throwable cause) {
        super("resposta em formato inesperado", cause);
    }
}
