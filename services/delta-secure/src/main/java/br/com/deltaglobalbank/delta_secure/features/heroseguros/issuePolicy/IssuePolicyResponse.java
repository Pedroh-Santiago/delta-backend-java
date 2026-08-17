package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Coverage;

public record IssuePolicyResponse(
    String ticket,
    String url,
    String price,
    List<Coverage> coverages,
    String termoAdesaoUrl,
    String sworksStatus,
    String sworksGuidDocumento,
    String sworksIdentificadorProcesso
) {
    public IssuePolicyResponse(String ticket, String url, String price, List<Coverage> coverages) {
        this(ticket, url, price, coverages, null, null, null, null);
    }
}
