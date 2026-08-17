package br.com.deltaglobalbank.delta_secure.features.sworks;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDocumentDispatchNotifier implements DocumentDispatchNotifier {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void notify(DocumentDispatchResult result) {
        if (result instanceof DocumentDispatchResult.Success success) {
            log.info(
                "envio ao sworks concluido idProposal={} identificador={} nomeArquivo={} guidDocumento={} verificacao={}",
                success.idProposal(),
                success.identificador(),
                success.nomeArquivo(),
                success.guidDocumento() != null ? success.guidDocumento() : "nao-identificado",
                success.verification().getClass().getSimpleName()
            );
        } else if (result instanceof DocumentDispatchResult.Failure failure) {
            log.error(
                "envio ao sworks falhou idProposal={} identificador={} nomeArquivo={} error={} message={}",
                failure.idProposal(),
                failure.identificador(),
                failure.nomeArquivo(),
                failure.error(),
                failure.message()
            );
        }
    }
}
