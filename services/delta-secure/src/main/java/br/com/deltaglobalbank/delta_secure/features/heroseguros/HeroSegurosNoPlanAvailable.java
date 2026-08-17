package br.com.deltaglobalbank.delta_secure.features.heroseguros;

public final class HeroSegurosNoPlanAvailable extends HeroSegurosException {

    public HeroSegurosNoPlanAvailable() {
        super("nenhum plano disponível para os valores informados");
    }
}
