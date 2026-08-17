package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksInvalidResponse extends SWorksException {
    public SWorksInvalidResponse(Throwable cause) {
        super("resposta do SWorks em formato inesperado", cause);
    }
}
