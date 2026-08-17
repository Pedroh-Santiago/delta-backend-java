package br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation;

import br.com.deltaglobalbank.delta_secure.domain.policy.ConvenioProductResolver;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.PrestamistaPolicyProperties;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosQuotationClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationResponse;
import feign.codec.DecodeException;
import org.springframework.stereotype.Service;

@Service
public class QuotationUseCase {

    private final HeroSegurosQuotationClient quotationClient;
    private final TokenService tokenService;
    private final PrestamistaPolicyProperties properties;

    public QuotationUseCase(
        HeroSegurosQuotationClient quotationClient,
        TokenService tokenService,
        PrestamistaPolicyProperties properties
    ) {
        this.quotationClient = quotationClient;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    public QuotationResponse execute(QuotationRequest request) {
        String authorization = "Bearer " + tokenService.getToken(request.convenio());
        int typeOfProduct = ConvenioProductResolver.resolve(request.convenio(), properties.typeOfProduct());

        HeroSegurosQuotationRequest quotationRequest = QuotationMappers.toHeroSegurosQuotationRequest(request, typeOfProduct);
        HeroSegurosQuotationResponse quotationResponse;
        try {
            quotationResponse = quotationClient.quote(authorization, quotationRequest);
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }
        return QuotationMappers.toQuotationResponse(quotationResponse);
    }
}
