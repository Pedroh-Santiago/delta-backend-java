package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.Sleeper;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksAttachDocumentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SWorksDocumentSender {

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MILLIS = 500L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SWorksDocumentClient client;
    private final SWorksBearerExecutor bearer;
    private final DocumentBase64Encoder encoder;
    private final SWorksDocumentVerifier verifier;
    private final Sleeper sleeper;
    private final SWorksProperties properties;

    public SWorksDocumentSender(
        SWorksDocumentClient client,
        SWorksBearerExecutor bearer,
        DocumentBase64Encoder encoder,
        SWorksDocumentVerifier verifier,
        Sleeper sleeper,
        SWorksProperties properties
    ) {
        this.client = client;
        this.bearer = bearer;
        this.encoder = encoder;
        this.verifier = verifier;
        this.sleeper = sleeper;
        this.properties = properties;
    }

    public String send(String identificador, String nomeArquivo, byte[] content) {
        String bytesBase64 = encoder.encode(content);
        String formulario = properties.documento().formulario();
        SWorksAttachDocumentRequest request = new SWorksAttachDocumentRequest(
            nomeArquivo,
            bytesBase64,
            formulario != null && !formulario.isBlank() ? formulario : null
        );

        SWorksUnavailable ultimaFalha = null;

        for (int tentativa = 0; tentativa < MAX_ATTEMPTS; tentativa++) {
            if (tentativa > 0) {
                sleeper.sleep(backoffMillis(tentativa));
                if (jaAnexado(identificador, nomeArquivo)) {
                    log.warn(
                        "documento ja estava anexado apos falha, nao reenviando identificador={} nomeArquivo={} tentativa={}",
                        identificador, nomeArquivo, tentativa
                    );
                    return bytesBase64;
                }
            }

            try {
                bearer.execute(authorization -> {
                    client.attachDocument(authorization, identificador, request);
                    return null;
                });
                return bytesBase64;
            } catch (SWorksUnavailable ex) {
                ultimaFalha = ex;
                log.warn(
                    "falha temporaria ao anexar documento identificador={} nomeArquivo={} tentativa={} status={}",
                    identificador, nomeArquivo, tentativa + 1, ex.getStatus()
                );
            }
        }

        log.error(
            "esgotadas as {} tentativas de anexar documento identificador={} nomeArquivo={}",
            MAX_ATTEMPTS, identificador, nomeArquivo
        );
        throw ultimaFalha != null ? ultimaFalha : new SWorksUnavailable(0, "envio não foi tentado");
    }

    private boolean jaAnexado(String identificador, String nomeArquivo) {
        try {
            return !verifier.findAttachments(identificador, nomeArquivo).isEmpty();
        } catch (RuntimeException ex) {
            log.warn(
                "nao foi possivel checar anexos antes do reenvio identificador={} nomeArquivo={}: {}",
                identificador, nomeArquivo, ex.getMessage()
            );
            return false;
        }
    }

    private long backoffMillis(int tentativa) {
        return INITIAL_BACKOFF_MILLIS << (tentativa - 1);
    }
}
