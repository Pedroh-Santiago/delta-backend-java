package br.com.deltaglobalbank.domain.approveInternalTransference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece.ApproveInternalTransferenceRequest;
import br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece.ApproveInternalTransferenceUseCase;
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.TefPublisher;
import org.junit.jupiter.api.Test;

class ApproveInternalTransferenceTest {

    private final InternalTransferenceRepository repository = mock(InternalTransferenceRepository.class);
    private final TefPublisher tefPublisher = mock(TefPublisher.class);
    private final ApproveInternalTransferenceUseCase useCase =
        new ApproveInternalTransferenceUseCase(repository, tefPublisher);

    @Test
    void deveAprovarTefsComSucesso() {
        UUID id = UUID.randomUUID();
        InternalTransference transference = new InternalTransference(
            id, 123456L, 1L, 10050, PaymentsStatus.WAITING, "Teste", Instant.now(), UUID.randomUUID()
        );

        when(repository.findById(id)).thenReturn(transference);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApproveInternalTransferenceRequest request = new ApproveInternalTransferenceRequest(List.of(id));
        List<UUID> result = useCase.execute(request);

        assertEquals(1, result.size());
        assertEquals(id, result.get(0));
    }

    @Test
    void deveIgnorarTefQueNaoExiste() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(null);

        ApproveInternalTransferenceRequest request = new ApproveInternalTransferenceRequest(List.of(id));
        List<UUID> result = useCase.execute(request);

        assertEquals(0, result.size());
    }

    @Test
    void devePublicarMensagemNaFilaAoAprovarTef() {
        UUID id = UUID.randomUUID();
        InternalTransference transference = new InternalTransference(
            id, 123456L, 1L, 10050, PaymentsStatus.WAITING, "Teste", Instant.now(), UUID.randomUUID()
        );

        when(repository.findById(id)).thenReturn(transference);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApproveInternalTransferenceRequest request = new ApproveInternalTransferenceRequest(List.of(id));
        useCase.execute(request);

        verify(tefPublisher).publishApproval(id);
    }

    @Test
    void deveRejeitarTefJaAprovada() {
        UUID id = UUID.randomUUID();
        InternalTransference transference = new InternalTransference(
            id, 123456L, 1L, 10050, PaymentsStatus.APPROVED, "Teste", Instant.now(), UUID.randomUUID()
        );

        when(repository.findById(id)).thenReturn(transference);

        ApproveInternalTransferenceRequest request = new ApproveInternalTransferenceRequest(List.of(id));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }
}
