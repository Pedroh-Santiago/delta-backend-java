package br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation;

import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Coverage;

public record QuotationResponse(
    String planName,
    String price,
    List<Coverage> coverages
) {
}
