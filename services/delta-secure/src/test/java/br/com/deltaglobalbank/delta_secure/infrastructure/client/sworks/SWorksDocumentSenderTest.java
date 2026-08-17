package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAccessDenied;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksDocumentRejected;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidDocument;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksProcessNotFound;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.Sleeper;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksAttachDocumentRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksProcessDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SWorksDocumentSenderTest {

    private final SWorksDocumentClient client = mock(SWorksDocumentClient.class);
    private final SWorksTokenService tokenService = mock(SWorksTokenService.class);
    private final SWorksDocumentVerifier verifier = mock(SWorksDocumentVerifier.class);
    private final List<Long> esperas = new ArrayList<>();

    private final String identificador = "123e4567-e89b-12d3-a456-426614174000";
    private final String nomeArquivo = "termo-adesao.pdf";
    private final byte[] pdf = "%PDF-1.5 termo de adesão".getBytes(StandardCharsets.UTF_8);
    private final String base64Esperado = Base64.getEncoder().encodeToString(pdf);

    private final SWorksDocumentSender sender = new SWorksDocumentSender(
        client,
        new SWorksBearerExecutor(tokenService),
        new DocumentBase64Encoder(),
        verifier,
        millis -> esperas.add(millis),
        new SWorksProperties("https://host/SWorks.WebApi", null, true, null, new SWorksDocumentSettings("SEGURO PRESTAMISTA"))
    );

    SWorksDocumentSenderTest() {
        when(tokenService.getToken()).thenReturn("token-1");
        when(verifier.findAttachments(any(), any())).thenReturn(List.of());
    }

    @Test
    void enviaDeUmaVezQuandoOSworksAceita() {
        assertEquals(base64Esperado, sender.send(identificador, nomeArquivo, pdf));

        verify(client, times(1)).attachDocument(any(), eq(identificador), any());
        assertTrue(esperas.isEmpty());
    }

    @Test
    void montaOCorpoComBase64PuroQueDecodificaParaOArquivoOriginal() {
        sender.send(identificador, nomeArquivo, pdf);

        ArgumentCaptor<SWorksAttachDocumentRequest> enviado = ArgumentCaptor.forClass(SWorksAttachDocumentRequest.class);
        verify(client).attachDocument(any(), any(), enviado.capture());

        assertEquals(nomeArquivo, enviado.getValue().nomeArquivo());
        assertTrue(enviado.getValue().formatoOriginal());
        assertEquals("SEGURO PRESTAMISTA", enviado.getValue().formulario(), "tipificação do documento");
        assertFalse(enviado.getValue().bytesBase64().startsWith("data:"));
        assertArrayEquals(pdf, Base64.getDecoder().decode(enviado.getValue().bytesBase64()));
    }

    @Test
    void reenviaDepoisDe5xxESucedeNaSegundaTentativa() {
        int[] chamadas = {0};
        doAnswer(invocation -> {
            chamadas[0]++;
            if (chamadas[0] == 1) {
                throw new SWorksUnavailable(502, "bad gateway");
            }
            return null;
        }).when(client).attachDocument(any(), any(), any());

        assertEquals(base64Esperado, sender.send(identificador, nomeArquivo, pdf));

        assertEquals(2, chamadas[0]);
        assertEquals(List.of(500L), esperas);
    }

    @Test
    void esgotaTresTentativasEm5xxEPropagaAFalhaComBackoffCrescente() {
        doThrow(new SWorksUnavailable(500, "erro interno")).when(client).attachDocument(any(), any(), any());

        SWorksUnavailable erro = assertThrows(SWorksUnavailable.class, () -> sender.send(identificador, nomeArquivo, pdf));

        assertEquals(500, erro.getStatus());
        verify(client, times(3)).attachDocument(any(), eq(identificador), any());
        assertEquals(List.of(500L, 1000L), esperas);
    }

    @Test
    void naoReenviaQuandoOAnexoJaEntrouApesarDaFalha() {
        doThrow(new SWorksUnavailable(504, "gateway timeout")).when(client).attachDocument(any(), any(), any());
        when(verifier.findAttachments(identificador, nomeArquivo)).thenReturn(
            List.of(new SWorksProcessDocument("guid-1", nomeArquivo, null, null, null))
        );

        assertEquals(base64Esperado, sender.send(identificador, nomeArquivo, pdf));

        verify(client, times(1)).attachDocument(any(), eq(identificador), any());
    }

    @Test
    void reenviaQuandoNaoConsegueChecarSeOAnexoJaEntrou() {
        int[] chamadas = {0};
        doAnswer(invocation -> {
            chamadas[0]++;
            if (chamadas[0] == 1) {
                throw new SWorksUnavailable(500, "erro interno");
            }
            return null;
        }).when(client).attachDocument(any(), any(), any());
        when(verifier.findAttachments(any(), any())).thenThrow(new SWorksAccessDenied("sem ProcessoAcesso"));

        assertEquals(base64Esperado, sender.send(identificador, nomeArquivo, pdf));

        assertEquals(2, chamadas[0]);
    }

    @Test
    void quatrocentosSobeNaPrimeiraTentativaSemRetry() {
        doThrow(new SWorksDocumentRejected(400, "base64 inválido")).when(client).attachDocument(any(), any(), any());

        assertThrows(SWorksDocumentRejected.class, () -> sender.send(identificador, nomeArquivo, pdf));

        verify(client, times(1)).attachDocument(any(), eq(identificador), any());
        assertTrue(esperas.isEmpty());
    }

    @Test
    void quatrocentosETresSobeNaPrimeiraTentativaSemRetry() {
        doThrow(new SWorksAccessDenied("sem ProcessoEscrita")).when(client).attachDocument(any(), any(), any());

        assertThrows(SWorksAccessDenied.class, () -> sender.send(identificador, nomeArquivo, pdf));

        verify(client, times(1)).attachDocument(any(), eq(identificador), any());
    }

    @Test
    void quatrocentosEQuatroSobeNaPrimeiraTentativaSemRetry() {
        doThrow(new SWorksProcessNotFound("processo inexistente")).when(client).attachDocument(any(), any(), any());

        assertThrows(SWorksProcessNotFound.class, () -> sender.send(identificador, nomeArquivo, pdf));

        verify(client, times(1)).attachDocument(any(), eq(identificador), any());
    }

    @Test
    void rejeitaConteudoVazioAntesDeChamarOSworks() {
        assertThrows(SWorksInvalidDocument.class, () -> sender.send(identificador, nomeArquivo, new byte[0]));

        verify(client, never()).attachDocument(any(), any(), any());
    }
}
