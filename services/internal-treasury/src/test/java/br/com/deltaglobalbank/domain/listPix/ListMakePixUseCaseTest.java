package br.com.deltaglobalbank.domain.listPix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.features.listPix.ListPixResponse;
import br.com.deltaglobalbank.internal_treasury.features.listPix.ListPixUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class ListMakePixUseCaseTest {

    private final MakePixRepository repository = mock(MakePixRepository.class);
    private final ListPixUseCase useCase = new ListPixUseCase(repository);

    @Test
    void deveRetornarApenasWaitingQuandoFiltroNaoInformado() {
        MakePix transference = new MakePix(
            UUID.randomUUID(), 123456L, "12", "1234", "214421",
            PaymentsStatus.WAITING, "214214", "Teste", 10050L, Instant.now()
        );

        PageImpl<MakePix> page = new PageImpl<>(List.of(transference));
        when(repository.findByStatus(eq(PaymentsStatus.WAITING), any())).thenReturn(page);

        ListPixResponse response = useCase.execute(0, 10, "waiting");

        assertEquals(1, response.content().size());
        assertEquals("WAITING", response.content().get(0).status());
    }

    @Test
    void deveRetornarApprovedQuandoFiltroForApproved() {
        MakePix transference = new MakePix(
            UUID.randomUUID(), 123456L, "12", "1234", "214421",
            PaymentsStatus.APPROVED, "214214", "Teste", 10050L, Instant.now()
        );

        PageImpl<MakePix> page = new PageImpl<>(List.of(transference));
        when(repository.findByStatus(eq(PaymentsStatus.APPROVED), any())).thenReturn(page);

        ListPixResponse response = useCase.execute(0, 10, "approved");

        assertEquals(1, response.content().size());
        assertEquals("APPROVED", response.content().get(0).status());
    }

    @Test
    void deveRetornarDeniedQuandoFiltroForDenied() {
        MakePix transference = new MakePix(
            UUID.randomUUID(), 123456L, "12", "1234", "214421",
            PaymentsStatus.DENIED, "214214", "Teste", 10050L, Instant.now()
        );

        PageImpl<MakePix> page = new PageImpl<>(List.of(transference));
        when(repository.findByStatus(eq(PaymentsStatus.DENIED), any())).thenReturn(page);

        ListPixResponse response = useCase.execute(0, 10, "denied");

        assertEquals(1, response.content().size());
        assertEquals("DENIED", response.content().get(0).status());
    }

    @Test
    void deveRetornarTodosQuandoFiltroForAll() {
        MakePix transference = new MakePix(
            UUID.randomUUID(), 123456L, "12", "1234", "214421",
            PaymentsStatus.WAITING, "214214", "Teste", 10050L, Instant.now()
        );

        PageImpl<MakePix> page = new PageImpl<>(List.of(transference));
        when(repository.findAll(any())).thenReturn(page);

        ListPixResponse response = useCase.execute(0, 10, "all");

        assertEquals(1, response.content().size());
        assertEquals("WAITING", response.content().get(0).status());
    }
}
