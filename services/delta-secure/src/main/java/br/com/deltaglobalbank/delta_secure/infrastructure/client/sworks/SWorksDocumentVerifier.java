package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import java.util.Base64;
import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification.Confirmed;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification.Inconclusive;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification.Mismatch;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification.Skipped;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksProcessDocument;
import org.springframework.stereotype.Component;

@Component
public class SWorksDocumentVerifier {

    private final SWorksDocumentClient client;
    private final SWorksBearerExecutor bearer;
    private final SWorksProperties properties;

    public SWorksDocumentVerifier(SWorksDocumentClient client, SWorksBearerExecutor bearer, SWorksProperties properties) {
        this.client = client;
        this.bearer = bearer;
        this.properties = properties;
    }

    public List<SWorksProcessDocument> findAttachments(String identificador, String nomeArquivo) {
        return bearer.execute(authorization -> client.getProcess(authorization, identificador))
            .documentos()
            .stream()
            .filter(doc -> {
                String nome = doc.nome();
                String trimmedNome = nome != null ? nome.trim() : null;
                return trimmedNome != null && trimmedNome.equalsIgnoreCase(nomeArquivo.trim());
            })
            .toList();
    }

    public DocumentVerification verify(String identificador, String nomeArquivo, String bytesBase64Enviado) {
        if (!properties.verifyAfterUpload()) {
            return new Skipped();
        }

        List<String> candidatos = findAttachments(identificador, nomeArquivo).stream()
            .map(SWorksProcessDocument::identificador)
            .filter(id -> id != null)
            .toList();
        if (candidatos.isEmpty()) {
            return new Inconclusive("nenhum anexo chamado " + nomeArquivo + " consta no processo");
        }

        byte[] esperado = decode(bytesBase64Enviado);
        if (esperado == null) {
            return new Inconclusive("o base64 enviado não pôde ser decodificado para conferência");
        }

        String divergenciaGuid = null;
        String divergenciaMotivo = null;
        String naoConferido = null;

        for (String guid : candidatos) {
            String recebido = bearer.execute(authorization -> client.getDocument(authorization, identificador, guid)).base64();
            if (recebido == null || recebido.isBlank()) {
                naoConferido = "documento " + guid + " voltou sem o campo Base64";
                continue;
            }
            byte[] gravado = decode(recebido);
            if (gravado == null) {
                naoConferido = "o Base64 do documento " + guid + " está malformado no SWorks";
                continue;
            }
            if (java.util.Arrays.equals(gravado, esperado)) {
                return new Confirmed(guid);
            }
            divergenciaGuid = guid;
            divergenciaMotivo = "conteúdo divergente: enviamos " + esperado.length + " bytes e o SWorks gravou " + gravado.length;
        }
        if (divergenciaGuid != null) {
            return new Mismatch(divergenciaGuid, divergenciaMotivo);
        }
        return new Inconclusive(naoConferido != null ? naoConferido : "não foi possível conferir os anexos encontrados");
    }

    private byte[] decode(String value) {
        try {
            return Base64.getMimeDecoder().decode(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
