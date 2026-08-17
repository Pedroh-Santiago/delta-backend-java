package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksProcessNotFound extends SWorksException {
    public SWorksProcessNotFound(String detail) {
        super("processo não encontrado no SWorks (HTTP 404): " + detail);
    }
}
