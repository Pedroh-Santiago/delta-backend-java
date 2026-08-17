package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksAuthenticationFailed extends SWorksException {
    private final int status;
    private final String detail;

    public SWorksAuthenticationFailed(int status, String detail, Throwable cause) {
        super(buildMessage(status, detail), cause);
        this.status = status;
        this.detail = detail;
    }

    public SWorksAuthenticationFailed(int status, String detail) {
        this(status, detail, null);
    }

    public SWorksAuthenticationFailed(int status) {
        this(status, "", null);
    }

    private static String buildMessage(int status, String detail) {
        String safeDetail = (detail == null || detail.isBlank()) ? "sem corpo" : detail;
        return "falha na autenticação no SWorks (HTTP " + status + "): " + safeDetail;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
