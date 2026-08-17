package br.com.deltaglobalbank.delta_secure.features.heroseguros;

public final class HeroSegurosUnavailable extends HeroSegurosException {

    public HeroSegurosUnavailable() {
        super("parceiro indisponível");
    }
}
