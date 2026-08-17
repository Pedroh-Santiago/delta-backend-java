package br.com.deltaglobalbank.delta_secure.domain.sworks;

public sealed interface DocumentDispatchDecision permits DocumentDispatchDecision.Rejected, DocumentDispatchDecision.Accepted {

    record Rejected(String reason) implements DocumentDispatchDecision {
    }

    record Accepted(String guidDocumento) implements DocumentDispatchDecision {
    }
}
