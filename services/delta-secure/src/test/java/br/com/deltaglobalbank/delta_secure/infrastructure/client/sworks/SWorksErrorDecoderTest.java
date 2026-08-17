package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAccessDenied;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksDocumentRejected;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksProcessNotFound;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksTokenExpired;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

class SWorksErrorDecoderTest {

    private final SWorksErrorDecoder decoder = new SWorksErrorDecoder();

    @Test
    void quatrocentosEUmViraTokenExpiradoParaPermitirUmaReAutenticacao() {
        assertInstanceOf(SWorksTokenExpired.class, decode(401, null));
    }

    @Test
    void quatrocentosETresViraAcessoNegadoECitaAPermissaoQueFalta() {
        SWorksAccessDenied erro = assertInstanceOf(SWorksAccessDenied.class, decode(403, null));

        assertTrue(erro.getMessage().contains("ProcessoEscrita"));
    }

    @Test
    void quatrocentosEQuatroViraProcessoNaoEncontrado() {
        assertInstanceOf(SWorksProcessNotFound.class, decode(404, null));
    }

    @Test
    void quatrocentosViraDocumentoRecusadoPreservandoOCorpoDoErro() {
        SWorksDocumentRejected erro = assertInstanceOf(SWorksDocumentRejected.class, decode(400, "{\"erro\":\"base64 inválido\"}"));

        assertEquals(400, erro.getStatus());
        assertTrue(erro.getDetail().contains("base64 inválido"));
    }

    @Test
    void outros4xxTambemViramDocumentoRecusadoSemRetry() {
        SWorksDocumentRejected erro = assertInstanceOf(SWorksDocumentRejected.class, decode(422, "entidade inválida"));

        assertEquals(422, erro.getStatus());
    }

    @Test
    void cincoXXViraIndisponivelParaHabilitarORetryComBackoff() {
        assertEquals(500, assertInstanceOf(SWorksUnavailable.class, decode(500, null)).getStatus());
        assertEquals(503, assertInstanceOf(SWorksUnavailable.class, decode(503, null)).getStatus());
    }

    @Test
    void respostaSemCorpoNaoQuebraODecoder() {
        SWorksDocumentRejected erro = assertInstanceOf(SWorksDocumentRejected.class, decode(400, null));

        assertEquals("sem corpo", erro.getDetail());
    }

    private Exception decode(int status, String body) {
        Request request = Request.create(
            Request.HttpMethod.PUT,
            "https://sworkshml-delta.simply.com.br/SWorks.WebApi/api/v1/Processo/abc/documentos",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response.Builder builder = Response.builder()
            .status(status)
            .reason("erro")
            .request(request)
            .headers(Map.of());
        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return decoder.decode("SWorksDocumentClient#attachDocument(String,String,SWorksAttachDocumentRequest)", builder.build());
    }
}
