package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

public record SWorksProcessSettings(
    Integer codigoWorkflow,
    String cdProduto,
    String tipoOperacao
) {
}
