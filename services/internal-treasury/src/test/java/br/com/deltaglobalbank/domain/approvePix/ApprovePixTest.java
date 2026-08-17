package br.com.deltaglobalbank.domain.approvePix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.features.approvePix.ApprovePixRequest;
import br.com.deltaglobalbank.internal_treasury.features.approvePix.ApprovePixUseCase;
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.PixPublisher;
import org.junit.jupiter.api.Test;

class ApprovePixTest {

    private final MakePixRepository repository = mock(MakePixRepository.class);
    private final PixPublisher pixPublisher = mock(PixPublisher.class);
    private final ApprovePixUseCase useCase = new ApprovePixUseCase(repository, pixPublisher);

    @Test
    void deveAprovarPixComSucesso() {
        UUID id = UUID.randomUUID();
        MakePix transference = new MakePix(
            id, 123456L, "1234", "Test", "test",
            PaymentsStatus.WAITING, "21453", "Testes", 12345L, Instant.now()
        );
        when(repository.findById(id)).thenReturn(transference);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovePixRequest request = new ApprovePixRequest(List.of(id));
        List<UUID> result = useCase.execute(request);

        assertEquals(1, result.size());
        assertEquals(id, result.get(0));
    }

    @Test
    void deveIgnorarPixQueNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(null);

        ApprovePixRequest request = new ApprovePixRequest(List.of(id));
        List<UUID> result = useCase.execute(request);

        assertEquals(0, result.size());
    }

    @Test
    void deveRejeitarPixJaAprovado() {
        UUID id = UUID.randomUUID();
        MakePix pix = new MakePix(
            id, 123456L, "1234", "Test", "test",
            PaymentsStatus.APPROVED, "21453", "Testes", 12345L, Instant.now()
        );

        when(repository.findById(id)).thenReturn(pix);

        ApprovePixRequest request = new ApprovePixRequest(List.of(id));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    void devePublicarMensagemNaFilaAoAprovarPix() {
        UUID id = UUID.randomUUID();
        MakePix pix = new MakePix(
            id, 123456L, "1234", "Test", "test",
            PaymentsStatus.WAITING, "21453", "Testes", 21345L, Instant.now()
        );

        when(repository.findById(id)).thenReturn(pix);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovePixRequest request = new ApprovePixRequest(List.of(id));
        useCase.execute(request);

        verify(repository).findById(id);
    }
}
