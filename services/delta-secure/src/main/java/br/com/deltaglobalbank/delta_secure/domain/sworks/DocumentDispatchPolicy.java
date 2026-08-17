package br.com.deltaglobalbank.delta_secure.domain.sworks;

public final class DocumentDispatchPolicy {

    private DocumentDispatchPolicy() {
    }

    public static DocumentDispatchDecision decide(DocumentVerification verificacao) {
        if (verificacao instanceof DocumentVerification.Mismatch mismatch) {
            return new DocumentDispatchDecision.Rejected(mismatch.reason());
        }
        if (verificacao instanceof DocumentVerification.Inconclusive) {
            return new DocumentDispatchDecision.Accepted(null);
        }
        if (verificacao instanceof DocumentVerification.Confirmed confirmed) {
            return new DocumentDispatchDecision.Accepted(confirmed.guidDocumento());
        }
        if (verificacao instanceof DocumentVerification.Skipped) {
            return new DocumentDispatchDecision.Accepted(null);
        }
        throw new IllegalStateException("DocumentVerification desconhecido: " + verificacao);
    }
}
