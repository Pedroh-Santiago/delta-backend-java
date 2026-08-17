package br.com.deltaglobalbank.domain.listInternal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference.ListInternalTransferenceResponse;
import br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference.ListInternalTransferenceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class ListInternalTransferenceUseCaseTest {

    private final InternalTransferenceRepository repository = mock(InternalTransferenceRepository.class);
    private final ListInternalTransferenceUseCase useCase = new ListInternalTransferenceUseCase(repository);

    @Test
    void deveRetornarApenasWaitingQuandoFiltroNaoInformado() {
        InternalTransference transference = new InternalTransference(
            UUID.randomUUID(), 123456L, 1L, 10050, PaymentsStatus.WAITING, "Teste", Instant.now(), UUID.randomUUID()
        );

        PageImpl<InternalTransference> page = new PageImpl<>(List.of(transference));
        when(repository.findByStatus(eq(PaymentsStatus.WAITING), any())).thenReturn(page);

        ListInternalTransferenceResponse response = useCase.execute(0, 10, "waiting");

        assertEquals(1, response.content().size());
        assertEquals("WAITING", response.content().get(0).status());
    }

    @Test
    void deveRetornarApprovedQuandoFiltroForApproved() {
        InternalTransference transference = new InternalTransference(
            UUID.randomUUID(), 123456L, 1L, 10050, PaymentsStatus.APPROVED, "Teste", Instant.now(), UUID.randomUUID()
        );

        PageImpl<InternalTransference> page = new PageImpl<>(List.of(transference));
        when(repository.findByStatus(eq(PaymentsStatus.APPROVED), any())).thenReturn(page);

        ListInternalTransferenceResponse response = useCase.execute(0, 10, "approved");

        assertEquals(1, response.content().size());
        assertEquals("APPROVED", response.content().get(0).status());
    }

    @Test
    void deveRetornarDeniedQuandoFiltroForDenied() {
        InternalTransference transference = new InternalTransference(
            UUID.randomUUID(), 123456L, 1L, 10050, PaymentsStatus.DENIED, "Teste", Instant.now(), UUID.randomUUID()
        );

        PageImpl<InternalTransference> page = new PageImpl<>(List.of(transference));
        when(repository.findByStatus(eq(PaymentsStatus.DENIED), any())).thenReturn(page);

        ListInternalTransferenceResponse response = useCase.execute(0, 10, "denied");

        assertEquals(1, response.content().size());
        assertEquals("DENIED", response.content().get(0).status());
    }

    @Test
    void deveRetornarTodosQuandoFiltroForAll() {
        InternalTransference transference = new InternalTransference(
            UUID.randomUUID(), 123456L, 1L, 10050, PaymentsStatus.APPROVED, "Teste", Instant.now(), UUID.randomUUID()
        );

        PageImpl<InternalTransference> page = new PageImpl<>(List.of(transference));
        when(repository.findAll(any())).thenReturn(page);

        ListInternalTransferenceResponse response = useCase.execute(0, 10, "all");

        assertEquals(1, response.content().size());
        assertEquals("APPROVED", response.content().get(0).status());
    }
}
