package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksDocumentRejected extends SWorksException {
    private final int status;
    private final String detail;

    public SWorksDocumentRejected(int status, String detail) {
        super("SWorks recusou o documento (HTTP " + status + "): " + detail);
        this.status = status;
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
