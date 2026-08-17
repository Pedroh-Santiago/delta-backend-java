package br.com.deltaglobalbank.delta_secure.domain.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DocumentDispatchPolicyTest {

    @Test
    void mismatchERejeitadoComOMotivoDaDivergencia() {
        DocumentDispatchDecision decisao = DocumentDispatchPolicy.decide(
            new DocumentVerification.Mismatch("guid-1", "conteudo divergente"));

        DocumentDispatchDecision.Rejected rejeitado = assertInstanceOf(DocumentDispatchDecision.Rejected.class, decisao);
        assertEquals("conteudo divergente", rejeitado.reason());
    }

    @Test
    void inconclusiveEAceitoSemGuid() {
        DocumentDispatchDecision decisao = DocumentDispatchPolicy.decide(
            new DocumentVerification.Inconclusive("nao foi possivel conferir"));

        DocumentDispatchDecision.Accepted aceito = assertInstanceOf(DocumentDispatchDecision.Accepted.class, decisao);
        assertNull(aceito.guidDocumento());
    }

    @Test
    void confirmedEAceitoComOGuidConfirmado() {
        DocumentDispatchDecision decisao = DocumentDispatchPolicy.decide(
            new DocumentVerification.Confirmed("guid-2"));

        DocumentDispatchDecision.Accepted aceito = assertInstanceOf(DocumentDispatchDecision.Accepted.class, decisao);
        assertEquals("guid-2", aceito.guidDocumento());
    }

    @Test
    void skippedEAceitoSemGuid() {
        DocumentDispatchDecision decisao = DocumentDispatchPolicy.decide(new DocumentVerification.Skipped());

        DocumentDispatchDecision.Accepted aceito = assertInstanceOf(DocumentDispatchDecision.Accepted.class, decisao);
        assertNull(aceito.guidDocumento());
    }
}
