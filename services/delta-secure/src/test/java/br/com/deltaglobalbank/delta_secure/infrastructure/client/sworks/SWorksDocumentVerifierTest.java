package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksDocumentResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksProcessDocument;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksProcessResponse;
import org.junit.jupiter.api.Test;

class SWorksDocumentVerifierTest {

    private final SWorksDocumentClient client = mock(SWorksDocumentClient.class);
    private final SWorksTokenService tokenService = mock(SWorksTokenService.class);

    private final String identificador = "123e4567-e89b-12d3-a456-426614174000";
    private final String nomeArquivo = "termo-adesao.pdf";
    private final byte[] pdf = "%PDF-1.5 termo de adesão".getBytes(StandardCharsets.UTF_8);
    private final String enviado = Base64.getEncoder().encodeToString(pdf);

    SWorksDocumentVerifierTest() {
        when(tokenService.getToken()).thenReturn("token-1");
    }

    @Test
    void confirmaQuandoOConteudoGravadoEIgualAoEnviado() {
        anexos(nota(nomeArquivo, "guid-1"));
        documento("guid-1", Base64.getEncoder().encodeToString(pdf));

        DocumentVerification resultado = verifier().verify(identificador, nomeArquivo, enviado);

        assertEquals("guid-1", assertInstanceOf(DocumentVerification.Confirmed.class, resultado).guidDocumento());
    }

    @Test
    void confirmaMesmoComQuebrasDeLinhaNoBase64DaResposta() {
        byte[] grande = new byte[4_096];
        for (int i = 0; i < grande.length; i++) {
            grande[i] = (byte) (i % 256);
        }
        anexos(nota(nomeArquivo, "guid-1"));
        documento("guid-1", Base64.getMimeEncoder().encodeToString(grande));

        DocumentVerification resultado = verifier().verify(
            identificador,
            nomeArquivo,
            Base64.getEncoder().encodeToString(grande)
        );

        assertInstanceOf(DocumentVerification.Confirmed.class, resultado);
    }

    @Test
    void acusaDivergenciaQuandoOConteudoGravadoEDiferente() {
        anexos(nota(nomeArquivo, "guid-1"));
        documento("guid-1", Base64.getEncoder().encodeToString("outro arquivo".getBytes(StandardCharsets.UTF_8)));

        DocumentVerification.Mismatch resultado = assertInstanceOf(
            DocumentVerification.Mismatch.class,
            verifier().verify(identificador, nomeArquivo, enviado)
        );

        assertEquals("guid-1", resultado.guidDocumento());
        assertTrue(resultado.reason().contains("divergente"));
    }

    @Test
    void confirmaQuandoHaVariosAnexosComOMesmoNomeEUmDelesCasa() {
        anexos(nota(nomeArquivo, "guid-antigo"), nota(nomeArquivo, "guid-novo"));
        documento("guid-antigo", Base64.getEncoder().encodeToString("versão antiga".getBytes(StandardCharsets.UTF_8)));
        documento("guid-novo", Base64.getEncoder().encodeToString(pdf));

        DocumentVerification resultado = verifier().verify(identificador, nomeArquivo, enviado);

        assertEquals("guid-novo", assertInstanceOf(DocumentVerification.Confirmed.class, resultado).guidDocumento());
    }

    @Test
    void casaONomeDoAnexoIgnorandoCaixaEEspacos() {
        anexos(nota("  TERMO-ADESAO.PDF ", "guid-1"));
        documento("guid-1", Base64.getEncoder().encodeToString(pdf));

        assertInstanceOf(DocumentVerification.Confirmed.class, verifier().verify(identificador, nomeArquivo, enviado));
    }

    @Test
    void ficaInconclusivoQuandoOAnexoNaoAparaceNaLista() {
        anexos(nota("outro-documento.pdf", "guid-9"));

        DocumentVerification.Inconclusive resultado = assertInstanceOf(
            DocumentVerification.Inconclusive.class,
            verifier().verify(identificador, nomeArquivo, enviado)
        );

        assertTrue(resultado.reason().contains("consta no processo"));
    }

    @Test
    void ficaInconclusivoQuandoODocumentoVoltaSemBase64EmVezDeAcusarDivergencia() {
        anexos(nota(nomeArquivo, "guid-1"));
        documento("guid-1", null);

        assertInstanceOf(DocumentVerification.Inconclusive.class, verifier().verify(identificador, nomeArquivo, enviado));
    }

    @Test
    void naoConsultaNadaQuandoAVerificacaoEstaDesligada() {
        DocumentVerification resultado = verifier(false).verify(identificador, nomeArquivo, enviado);

        assertInstanceOf(DocumentVerification.Skipped.class, resultado);
    }

    @Test
    void findAttachmentsFiltraPeloNomeDoArquivo() {
        anexos(nota(nomeArquivo, "guid-1"), nota("outro.pdf", "guid-2"));

        List<SWorksProcessDocument> encontrados = verifier().findAttachments(identificador, nomeArquivo);

        assertEquals(List.of("guid-1"), encontrados.stream().map(SWorksProcessDocument::identificador).toList());
    }

    private SWorksDocumentVerifier verifier() {
        return verifier(true);
    }

    private SWorksDocumentVerifier verifier(boolean verifyAfterUpload) {
        return new SWorksDocumentVerifier(
            client,
            new SWorksBearerExecutor(tokenService),
            new SWorksProperties(
                "https://host/SWorks.WebApi",
                new SWorksCredentials("u", "p", "password"),
                verifyAfterUpload,
                null,
                null
            )
        );
    }

    private void anexos(SWorksProcessDocument... documentos) {
        when(client.getProcess(any(), org.mockito.ArgumentMatchers.eq(identificador))).thenReturn(
            new SWorksProcessResponse(identificador, null, null, null, List.of(documentos))
        );
    }

    private void documento(String guid, String base64) {
        when(client.getDocument(any(), org.mockito.ArgumentMatchers.eq(identificador), org.mockito.ArgumentMatchers.eq(guid)))
            .thenReturn(new SWorksDocumentResponse(guid, nomeArquivo, null, null, null, null, base64));
    }

    private SWorksProcessDocument nota(String nomeDocumento, String guid) {
        return new SWorksProcessDocument(guid, nomeDocumento, null, null, null);
    }
}
