package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksNotConfigured extends SWorksException {
    public SWorksNotConfigured(String missing) {
        super("configuração ausente do SWorks: " + missing);
    }
}
