package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification;
import br.com.deltaglobalbank.delta_secure.features.sworks.DocumentDispatchNotifier;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAuthenticationFailed;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksDocumentRejected;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentSender;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentVerifier;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessCreator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

class SendPolicyDocumentToSWorksUseCaseTest {

    private final SWorksDocumentSender sender = mock(SWorksDocumentSender.class);
    private final SWorksDocumentVerifier verifier = mock(SWorksDocumentVerifier.class);
    private final DocumentDispatchNotifier notifier = mock(DocumentDispatchNotifier.class);

    private final SWorksProcessCreator processCreator = mock(SWorksProcessCreator.class);

    private final SendPolicyDocumentToSWorksUseCase useCase =
        new SendPolicyDocumentToSWorksUseCase(sender, verifier, processCreator, notifier);

    private final byte[] pdf = "%PDF-1.5 termo".getBytes();

    private final SendPolicyDocumentCommand comando = new SendPolicyDocumentCommand(
        "PROP-9911", "TCK-4477", "123e4567-e89b-12d3-a456-426614174000", pdf
    );
    private final String base64 = Base64.getEncoder().encodeToString(pdf);
    private final String nomeEsperado = "termo-adesao-TCK-4477.pdf";

    private SendPolicyDocumentCommand comandoComIniciarProcesso() {
        return new SendPolicyDocumentCommand(
            comando.idProposal(), comando.ticket(), comando.identificadorProcesso(), comando.documentBytes(), true
        );
    }

    @Test
    void enviaOTermoENotificaSucessoQuandoAVerificacaoConfirma() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));

        DocumentDispatchResult.Success resultado =
            assertInstanceOf(DocumentDispatchResult.Success.class, useCase.execute(comando));

        assertEquals("guid-1", resultado.guidDocumento());
        assertEquals("PROP-9911", resultado.idProposal());
        assertEquals(nomeEsperado, resultado.nomeArquivo());
        verify(sender, times(1)).send(comando.identificadorProcesso(), nomeEsperado, pdf);
        verify(notifier, times(1)).notify(resultado);
    }

    @Test
    void notificaSomenteDepoisDeAnexarEConferir() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));

        useCase.execute(comando);

        InOrder inOrder = Mockito.inOrder(sender, verifier, notifier);
        inOrder.verify(sender).send(any(), any(), any());
        inOrder.verify(verifier).verify(any(), any(), any());
        inOrder.verify(notifier).notify(any());
    }

    @Test
    void iniciaOProcessoDepoisDeAnexarQuandoPedido() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));

        assertInstanceOf(
            DocumentDispatchResult.Success.class,
            useCase.execute(comandoComIniciarProcesso())
        );

        InOrder inOrder = Mockito.inOrder(sender, verifier, processCreator, notifier);
        inOrder.verify(sender).send(any(), any(), any());
        inOrder.verify(verifier).verify(any(), any(), any());
        inOrder.verify(processCreator).start(comando.identificadorProcesso());
        inOrder.verify(notifier).notify(any());
    }

    @Test
    void naoIniciaProcessoQueNaoFoiCriadoPorNos() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));

        useCase.execute(comando);

        verify(processCreator, never()).start(any());
    }

    @Test
    void falhaAoIniciarOProcessoViraFALHAPorqueOFluxoFicaParado() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));
        doThrow(new SWorksUnavailable(500, "erro interno")).when(processCreator).start(any());

        DocumentDispatchResult.Failure resultado = assertInstanceOf(
            DocumentDispatchResult.Failure.class,
            useCase.execute(comandoComIniciarProcesso())
        );

        assertEquals("SWorksProcessoNaoIniciado", resultado.error());
        verify(notifier, times(1)).notify(resultado);
    }

    @Test
    void naoIniciaOProcessoQuandoAVerificacaoAcusouDivergencia() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any()))
            .thenReturn(new DocumentVerification.Mismatch("guid-1", "conteúdo divergente"));

        useCase.execute(comandoComIniciarProcesso());

        verify(processCreator, never()).start(any());
    }

    @Test
    void divergenciaNaVerificacaoNotificaFalhaENaoSucesso() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Mismatch(
            "guid-1", "conteúdo divergente: enviamos 14 bytes e o SWorks gravou 9"
        ));

        DocumentDispatchResult.Failure resultado =
            assertInstanceOf(DocumentDispatchResult.Failure.class, useCase.execute(comando));

        assertEquals("SWorksDocumentMismatch", resultado.error());
        assertTrue(resultado.message().contains("divergente"));
        verify(notifier).notify(resultado);
    }

    @Test
    void verificacaoInconclusivaSegueComoSucessoPorqueOAnexoFoiAceito() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any()))
            .thenReturn(new DocumentVerification.Inconclusive("nenhum anexo apareceu em RecuperarObservacoesAnexos"));

        DocumentDispatchResult.Success resultado =
            assertInstanceOf(DocumentDispatchResult.Success.class, useCase.execute(comando));

        assertNull(resultado.guidDocumento());
        assertInstanceOf(DocumentVerification.Inconclusive.class, resultado.verification());
    }

    @Test
    void verificacaoDesligadaTambemESucesso() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Skipped());

        assertInstanceOf(DocumentDispatchResult.Success.class, useCase.execute(comando));
    }

    @Test
    void falhaDeAutenticacaoNoSworksNotificaFalhaENaoChegaAConferir() {
        when(sender.send(any(), any(), any()))
            .thenThrow(new SWorksAuthenticationFailed(500, "{\"detail\":\"Login Attempt Failed\"}"));

        DocumentDispatchResult.Failure resultado =
            assertInstanceOf(DocumentDispatchResult.Failure.class, useCase.execute(comando));

        assertEquals("SWorksAuthenticationFailed", resultado.error());
        assertTrue(resultado.message().contains("Login Attempt Failed"));
        verify(verifier, never()).verify(any(), any(), any());
        verify(notifier, times(1)).notify(resultado);
    }

    @Test
    void sworksIndisponivelAposOsRetriesNotificaFalhaComErrorEMessage() {
        when(sender.send(any(), any(), any())).thenThrow(new SWorksUnavailable(500, "erro interno"));

        DocumentDispatchResult.Failure resultado =
            assertInstanceOf(DocumentDispatchResult.Failure.class, useCase.execute(comando));

        assertEquals("SWorksUnavailable", resultado.error());
        assertTrue(resultado.message().contains("500"));
    }

    @Test
    void documentoRecusadoCarregaODetalheDoCorpoDeErroDoSworks() {
        when(sender.send(any(), any(), any())).thenThrow(new SWorksDocumentRejected(
            400, "O nome do arquivo não foi informado ou foi informado sem a extensão do arquivo."
        ));

        DocumentDispatchResult.Failure resultado =
            assertInstanceOf(DocumentDispatchResult.Failure.class, useCase.execute(comando));

        assertEquals("SWorksDocumentRejected", resultado.error());
        assertTrue(resultado.message().contains("nome do arquivo"));
    }

    @Test
    void erroInesperadoTambemNotificaFalha() {
        when(sender.send(any(), any(), any())).thenThrow(new IllegalStateException("bug qualquer"));

        DocumentDispatchResult.Failure resultado =
            assertInstanceOf(DocumentDispatchResult.Failure.class, useCase.execute(comando));

        assertEquals("IllegalStateException", resultado.error());
        verify(notifier, times(1)).notify(resultado);
    }

    @Test
    void falhaAoNotificarNaoApagaOResultadoDoEnvio() {
        when(sender.send(any(), any(), any())).thenReturn(base64);
        when(verifier.verify(any(), any(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));
        doThrow(new IllegalStateException("backoffice fora do ar")).when(notifier).notify(any());

        DocumentDispatchResult.Success resultado =
            assertInstanceOf(DocumentDispatchResult.Success.class, useCase.execute(comando));

        assertEquals("guid-1", resultado.guidDocumento());
    }

    @Test
    void confereOMesmoArquivoQueEnviou() {
        ArgumentCaptor<String> nomeEnviado = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nomeConferido = ArgumentCaptor.forClass(String.class);
        when(sender.send(any(), nomeEnviado.capture(), any())).thenReturn(base64);
        when(verifier.verify(any(), nomeConferido.capture(), any())).thenReturn(new DocumentVerification.Confirmed("guid-1"));

        useCase.execute(comando);

        assertEquals(nomeEnviado.getValue(), nomeConferido.getValue());
    }
}
