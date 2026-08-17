package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksAccessDenied extends SWorksException {
    public SWorksAccessDenied(String detail) {
        super("sem permissão no SWorks, confira ProcessoEscrita do usuário (HTTP 403): " + detail);
    }
}
