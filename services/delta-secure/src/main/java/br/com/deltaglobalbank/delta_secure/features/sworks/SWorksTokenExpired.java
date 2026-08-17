package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksTokenExpired extends SWorksException {
    public SWorksTokenExpired() {
        super("token do SWorks expirado ou inválido (HTTP 401)");
    }
}
