package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

public record Coverage(
    int id,
    String coverageName,
    String insuredAmount,
    Integer waitingPeriodDays
) {
}
