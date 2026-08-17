package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses;

import java.util.List;

public record ArbiPixTransferError(
    int idOrdemPagamento,
    String endToEnd,
    String statusOrdemPagamento,
    List<ArbiPixDescriptionErro> erros
) {
}
