package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import java.util.List;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksNotConfigured;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksCreateProcessRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksCreateProcessResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SWorksProcessCreator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SWorksProcessClient client;
    private final SWorksBearerExecutor bearer;
    private final SWorksProperties properties;

    public SWorksProcessCreator(SWorksProcessClient client, SWorksBearerExecutor bearer, SWorksProperties properties) {
        this.client = client;
        this.bearer = bearer;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void anunciarConfiguracao() {
        Integer codigoWorkflow = properties.processo().codigoWorkflow();
        if (codigoWorkflow == null) {
            log.warn(
                "SWorks: criacao de processo DESLIGADA. "
                    + "Defina sworks.processo.codigo-workflow (ou SWORKS_PROCESSO_CODIGO_WORKFLOW) para ligar."
            );
        } else {
            String cdProduto = properties.processo().cdProduto();
            String tipoOperacao = properties.processo().tipoOperacao();
            log.info(
                "SWorks: criacao de processo LIGADA no workflow {} (cdProduto={}, tipoOperacao={})",
                codigoWorkflow,
                (cdProduto == null || cdProduto.isBlank()) ? "nao informado" : cdProduto,
                (tipoOperacao == null || tipoOperacao.isBlank()) ? "nao informado" : tipoOperacao
            );
        }
    }

    public boolean habilitado() {
        return properties.processo().codigoWorkflow() != null;
    }

    public SWorksProcessSettings settings() {
        return properties.processo();
    }

    public SWorksCreateProcessResponse create(List<SWorksInputField> dadosEntrada) {
        Integer codigoWorkflow = properties.processo().codigoWorkflow();
        if (codigoWorkflow == null) {
            throw new SWorksNotConfigured("SWORKS_PROCESSO_CODIGO_WORKFLOW");
        }

        SWorksCreateProcessResponse resposta = bearer.execute(authorization ->
            client.createProcess(authorization, new SWorksCreateProcessRequest(codigoWorkflow, dadosEntrada))
        );
        if (resposta.identificador() == null || resposta.identificador().isBlank()) {
            throw new SWorksInvalidResponse(
                new IllegalStateException("criação de processo no workflow " + codigoWorkflow + " não devolveu Identificador")
            );
        }

        log.info(
            "processo criado no sworks codigoWorkflow={} identificador={} codigoProcesso={}",
            codigoWorkflow, resposta.identificador(), resposta.codigoProcesso()
        );
        return resposta;
    }

    public void start(String identificador) {
        bearer.execute(authorization -> {
            client.startProcess(authorization, identificador);
            return null;
        });
        log.info("processo iniciado no sworks identificador={}", identificador);
    }
}
