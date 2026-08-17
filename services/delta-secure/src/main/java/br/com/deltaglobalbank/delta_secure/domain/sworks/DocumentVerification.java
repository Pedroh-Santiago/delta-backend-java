package br.com.deltaglobalbank.delta_secure.domain.sworks;

public sealed interface DocumentVerification
    permits DocumentVerification.Confirmed, DocumentVerification.Mismatch, DocumentVerification.Inconclusive, DocumentVerification.Skipped {

    record Confirmed(String guidDocumento) implements DocumentVerification {
    }

    record Mismatch(String guidDocumento, String reason) implements DocumentVerification {
    }

    record Inconclusive(String reason) implements DocumentVerification {
    }

    record Skipped() implements DocumentVerification {
    }
}
