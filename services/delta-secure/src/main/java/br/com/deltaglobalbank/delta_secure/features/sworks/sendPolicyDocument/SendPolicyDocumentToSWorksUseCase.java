package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchDecision;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchPolicy;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification;
import br.com.deltaglobalbank.delta_secure.features.sworks.DocumentDispatchNotifier;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentSender;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentVerifier;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SendPolicyDocumentToSWorksUseCase {

    private final SWorksDocumentSender sender;
    private final SWorksDocumentVerifier verifier;
    private final SWorksProcessCreator processCreator;
    private final DocumentDispatchNotifier notifier;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public SendPolicyDocumentToSWorksUseCase(
        SWorksDocumentSender sender,
        SWorksDocumentVerifier verifier,
        SWorksProcessCreator processCreator,
        DocumentDispatchNotifier notifier
    ) {
        this.sender = sender;
        this.verifier = verifier;
        this.processCreator = processCreator;
        this.notifier = notifier;
    }

    public DocumentDispatchResult execute(SendPolicyDocumentCommand command) {
        String nomeArquivo = nomeArquivo(command.ticket());

        DocumentDispatchResult result;
        try {
            result = enviar(command, nomeArquivo);
        } catch (Exception ex) {
            log.error(
                "falha no envio ao sworks idProposal={} identificador={} nomeArquivo={} error={}",
                command.idProposal(), command.identificadorProcesso(), nomeArquivo, ex.getClass().getSimpleName(), ex
            );
            result = new DocumentDispatchResult.Failure(
                command.idProposal(),
                command.identificadorProcesso(),
                nomeArquivo,
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : "sem detalhe"
            );
        }

        notificar(result);
        return result;
    }

    private DocumentDispatchResult enviar(SendPolicyDocumentCommand command, String nomeArquivo) {
        String bytesBase64 = sender.send(command.identificadorProcesso(), nomeArquivo, command.documentBytes());
        DocumentVerification verificacao = verifier.verify(command.identificadorProcesso(), nomeArquivo, bytesBase64);

        DocumentDispatchDecision decisao = DocumentDispatchPolicy.decide(verificacao);
        if (decisao instanceof DocumentDispatchDecision.Rejected rejected) {
            return new DocumentDispatchResult.Failure(
                command.idProposal(),
                command.identificadorProcesso(),
                nomeArquivo,
                "SWorksDocumentMismatch",
                rejected.reason()
            );
        }

        DocumentDispatchDecision.Accepted accepted = (DocumentDispatchDecision.Accepted) decisao;
        if (verificacao instanceof DocumentVerification.Inconclusive inconclusive) {
            log.warn(
                "anexo aceito mas nao conferido idProposal={} identificador={} nomeArquivo={} motivo={}",
                command.idProposal(), command.identificadorProcesso(), nomeArquivo, inconclusive.reason()
            );
        }
        return iniciarSeNecessario(command, nomeArquivo, accepted.guidDocumento(), verificacao);
    }

    private DocumentDispatchResult iniciarSeNecessario(
        SendPolicyDocumentCommand command,
        String nomeArquivo,
        String guidDocumento,
        DocumentVerification verificacao
    ) {
        if (!command.iniciarProcesso()) {
            return sucesso(command, nomeArquivo, guidDocumento, verificacao);
        }

        try {
            processCreator.start(command.identificadorProcesso());
            return sucesso(command, nomeArquivo, guidDocumento, verificacao);
        } catch (Exception ex) {
            log.error(
                "documento anexado mas processo nao iniciado idProposal={} identificador={}",
                command.idProposal(), command.identificadorProcesso(), ex
            );
            return new DocumentDispatchResult.Failure(
                command.idProposal(),
                command.identificadorProcesso(),
                nomeArquivo,
                "SWorksProcessoNaoIniciado",
                ex.getMessage() != null ? ex.getMessage() : "falha ao iniciar o processo depois do anexo"
            );
        }
    }

    private DocumentDispatchResult sucesso(
        SendPolicyDocumentCommand command,
        String nomeArquivo,
        String guidDocumento,
        DocumentVerification verificacao
    ) {
        return new DocumentDispatchResult.Success(
            command.idProposal(),
            command.identificadorProcesso(),
            nomeArquivo,
            guidDocumento,
            verificacao
        );
    }

    private void notificar(DocumentDispatchResult result) {
        try {
            notifier.notify(result);
        } catch (RuntimeException ex) {
            log.error(
                "nao foi possivel notificar o desfecho idProposal={} identificador={}",
                result.idProposal(), result.identificador(), ex
            );
        }
    }

    private String nomeArquivo(String ticket) {
        return "termo-adesao-" + ticket + ".pdf";
    }
}
