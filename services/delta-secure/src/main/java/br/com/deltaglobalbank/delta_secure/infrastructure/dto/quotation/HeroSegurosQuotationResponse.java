package br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation;

import java.util.List;

public record HeroSegurosQuotationResponse(
    boolean success,
    List<HeroSegurosQuotationPlan> data
) {
}
