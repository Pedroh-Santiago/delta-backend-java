package br.com.deltaglobalbank.delta_secure.features.heroseguros;

public abstract sealed class HeroSegurosException extends RuntimeException
    permits
        HeroSegurosUnavailable,
        HeroSegurosRequestRejected,
        HeroSegurosInvalidResponse,
        HeroSegurosNoPlanAvailable,
        TermoAdesaoNaoEncontrado {

    protected HeroSegurosException(String message) {
        super(message);
    }

    protected HeroSegurosException(String message, Throwable cause) {
        super(message, cause);
    }
}
