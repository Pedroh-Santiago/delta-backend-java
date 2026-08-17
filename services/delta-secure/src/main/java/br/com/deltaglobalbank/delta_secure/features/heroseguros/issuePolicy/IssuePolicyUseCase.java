package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.delta_secure.domain.policy.ConvenioProductResolver;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosNoPlanAvailable;
import br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument.SendPolicyDocumentCommand;
import br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument.SendPolicyDocumentToSWorksUseCase;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosIssuePolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosQuotationClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksFieldFormats;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessCreator;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationPlan;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationResponse;
import feign.codec.DecodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IssuePolicyUseCase {

    private static final Logger logger = LoggerFactory.getLogger(IssuePolicyUseCase.class);

    private final HeroSegurosIssuePolicyClient issuePolicyClient;
    private final HeroSegurosQuotationClient quotationClient;
    private final TokenService tokenService;
    private final PrestamistaPolicyProperties properties;
    private final TermoAdesaoPdfService termoAdesaoPdfService;
    private final TermoAdesaoReferenceRepository termoAdesaoReferenceRepository;
    private final SendPolicyDocumentToSWorksUseCase sendPolicyDocumentToSWorksUseCase;
    private final SWorksProcessCreator sworksProcessCreator;
    private final String baseUrl;

    public IssuePolicyUseCase(
        HeroSegurosIssuePolicyClient issuePolicyClient,
        HeroSegurosQuotationClient quotationClient,
        TokenService tokenService,
        PrestamistaPolicyProperties properties,
        TermoAdesaoPdfService termoAdesaoPdfService,
        TermoAdesaoReferenceRepository termoAdesaoReferenceRepository,
        SendPolicyDocumentToSWorksUseCase sendPolicyDocumentToSWorksUseCase,
        SWorksProcessCreator sworksProcessCreator,
        @Value("${document-storage.base-url}") String baseUrl
    ) {
        this.issuePolicyClient = issuePolicyClient;
        this.quotationClient = quotationClient;
        this.tokenService = tokenService;
        this.properties = properties;
        this.termoAdesaoPdfService = termoAdesaoPdfService;
        this.termoAdesaoReferenceRepository = termoAdesaoReferenceRepository;
        this.sendPolicyDocumentToSWorksUseCase = sendPolicyDocumentToSWorksUseCase;
        this.sworksProcessCreator = sworksProcessCreator;
        this.baseUrl = baseUrl;
    }

    public IssuePolicyResponse execute(IssuePolicyRequest request) {
        String authorization = "Bearer " + tokenService.getToken(request.convenio());
        int typeOfProduct = ConvenioProductResolver.resolve(request.convenio(), properties.typeOfProduct());

        HeroSegurosQuotationRequest quotationRequest = IssuePolicyMappers.toHeroSegurosQuotationRequest(request, typeOfProduct);
        HeroSegurosQuotationResponse quotationResponse;
        try {
            quotationResponse = quotationClient.quote(authorization, quotationRequest);
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }
        HeroSegurosQuotationPlan plan = quotationResponse.data().stream().findFirst()
            .orElseThrow(HeroSegurosNoPlanAvailable::new);

        HeroSegurosPolicyRequest heroSegurosRequest = IssuePolicyMappers.toHeroSegurosPolicyRequest(
            request, plan.partnerPlanId(), typeOfProduct
        );
        HeroSegurosPolicyResponse heroSegurosResponse;
        try {
            heroSegurosResponse = issuePolicyClient.issuePolicy(authorization, heroSegurosRequest);
        } catch (DecodeException ex) {
            throw new HeroSegurosInvalidResponse(ex);
        }

        IssuePolicyResponse baseResponse = IssuePolicyMappers.toIssuePolicyResponse(heroSegurosResponse);
        String ticket = heroSegurosResponse.data().policy().ticket();
        byte[] termoAdesaoBytes;
        try {
            termoAdesaoBytes = termoAdesaoPdfService.generate(request, heroSegurosResponse.data());
        } catch (Exception ex) {
            logger.error("Erro ao gerar Termo de Adesão para o ticket " + ticket, ex);
            termoAdesaoBytes = null;
        }
        String termoAdesaoUrl = termoAdesaoBytes != null ? urlDoTermoAdesao(ticket) : null;

        if (heroSegurosResponse.data().id() != null) {
            persistirReferencia(ticket, heroSegurosResponse.data().id(), request);
        }

        DocumentDispatchResult envio = enviarAoSWorks(request, ticket, termoAdesaoBytes, heroSegurosResponse.data().id());

        String sworksStatus = envio == null ? null : (envio instanceof DocumentDispatchResult.Success ? "SUCESSO" : "FALHA");
        String sworksGuidDocumento = envio instanceof DocumentDispatchResult.Success success ? success.guidDocumento() : null;
        String sworksIdentificadorProcesso = envio == null ? null : envio.identificador();

        return new IssuePolicyResponse(
            baseResponse.ticket(),
            baseResponse.url(),
            baseResponse.price(),
            baseResponse.coverages(),
            termoAdesaoUrl,
            sworksStatus,
            sworksGuidDocumento,
            sworksIdentificadorProcesso
        );
    }

    private String urlDoTermoAdesao(String ticket) {
        return baseUrl + "/heroseguros/prestamista/policies/" + ticket + "/termo-adesao";
    }

    private void persistirReferencia(String ticket, int heroSegurosId, IssuePolicyRequest request) {
        try {
            termoAdesaoReferenceRepository.save(
                new TermoAdesaoReference(
                    UUID.randomUUID(),
                    ticket,
                    heroSegurosId,
                    request.convenio(),
                    request.externalId(),
                    Instant.now()
                )
            );
        } catch (Exception ex) {
            logger.error("Não foi possível salvar a referência do termo de adesão para o ticket " + ticket, ex);
        }
    }

    private DocumentDispatchResult enviarAoSWorks(
        IssuePolicyRequest request,
        String ticket,
        byte[] termoAdesaoBytes,
        Integer heroSegurosPolicyId
    ) {
        if (termoAdesaoBytes == null) {
            logger.error("Termo de Adesão não foi gerado para o ticket " + ticket + "; nada a enviar ao SWorks");
            return null;
        }

        try {
            String informado = request.identificadorProcessoSWorks() != null && !request.identificadorProcessoSWorks().isBlank()
                ? request.identificadorProcessoSWorks()
                : null;
            String identificador = informado != null ? informado : criarProcesso(request, ticket, heroSegurosPolicyId);
            if (identificador == null) {
                return null;
            }

            return sendPolicyDocumentToSWorksUseCase.execute(
                new SendPolicyDocumentCommand(
                    request.externalId() != null ? request.externalId() : ticket,
                    ticket,
                    identificador,
                    termoAdesaoBytes,
                    informado == null
                )
            );
        } catch (Exception ex) {
            logger.error("Erro inesperado no envio ao SWorks do ticket " + ticket, ex);
            return null;
        }
    }

    private String criarProcesso(IssuePolicyRequest request, String ticket, Integer heroSegurosPolicyId) {
        if (!sworksProcessCreator.habilitado()) {
            logger.info(
                "Envio ao SWorks não realizado para o ticket " + ticket + ": "
                    + "sem identificador no request e criação de processo desligada"
            );
            return null;
        }

        String proposta = SWorksFieldFormats.apenasSeNumerico(request.externalId());
        if (proposta == null && heroSegurosPolicyId != null) {
            proposta = heroSegurosPolicyId.toString();
        }
        if (proposta == null) {
            logger.warn(
                "Criando processo no SWorks sem o campo Proposta para o ticket {}: externalId={} não é numérico "
                    + "e a HeroSeguros não devolveu id. Se o workflow exigir Proposta, a criação será recusada.",
                ticket, request.externalId() != null ? request.externalId() : "nulo"
            );
        }

        var criado = sworksProcessCreator.create(
            SWorksProcessFieldsMapper.toSWorksInputFields(request, ticket, sworksProcessCreator.settings(), proposta)
        );
        logger.info(
            "Processo criado no SWorks para o ticket {}: identificador={} codigoProcesso={}",
            ticket, criado.identificador(), criado.codigoProcesso()
        );
        return criado.identificador();
    }
}
