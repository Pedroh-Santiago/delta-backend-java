package br.com.deltaglobalbank.delta_secure.features.sworks;

public final class SWorksUnavailable extends SWorksException {
    private final int status;
    private final String detail;

    public SWorksUnavailable(int status, String detail, Throwable cause) {
        super(buildMessage(status, detail), cause);
        this.status = status;
        this.detail = detail;
    }

    public SWorksUnavailable(int status, String detail) {
        this(status, detail, null);
    }

    private static String buildMessage(int status, String detail) {
        return status > 0
            ? "SWorks indisponível (HTTP " + status + "): " + detail
            : "SWorks inacessível: " + detail;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
