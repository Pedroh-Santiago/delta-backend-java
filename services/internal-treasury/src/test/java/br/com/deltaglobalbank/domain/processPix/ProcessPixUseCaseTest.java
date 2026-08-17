package br.com.deltaglobalbank.domain.processPix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PaysmartPixException;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixGateway;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixNotFoundException;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.features.processPix.ProcessPixUseCase;
import org.junit.jupiter.api.Test;

class ProcessPixUseCaseTest {

    private final MakePixRepository repository = mock(MakePixRepository.class);
    private final PixGateway paysmartGateway = mock(PixGateway.class);
    private final ProcessPixUseCase useCase = new ProcessPixUseCase(repository, paysmartGateway);

    @Test
    void deveProcessarPagamentoComSucesso() {
        UUID id = UUID.randomUUID();
        MakePix transference = new MakePix(
            id, 12345L, "123", "12", "12345",
            PaymentsStatus.APPROVED, "CORRENTE", "TESTE", 1000000L, Instant.now()
        );

        when(repository.findById(id)).thenReturn(transference);
        when(paysmartGateway.transferPix(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(1234L);

        useCase.execute(id);

        verify(repository).save(transference);
        assertEquals(PaymentsStatus.PAID, transference.status());
    }

    @Test
    void deveLancarExcecaoQuandoPixNaoForEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(null);

        assertThrows(PixNotFoundException.class, () -> useCase.execute(id));
    }

    @Test
    void naoDeveSalvarQuandoPaysmartFalhar() {
        UUID id = UUID.randomUUID();
        MakePix transference = new MakePix(
            id, 12345L, "123", "12", "12345",
            PaymentsStatus.WAITING, "CORRENTE", "TESTE", 1000000L, Instant.now()
        );

        when(repository.findById(id)).thenReturn(transference);
        when(paysmartGateway.transferPix(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong()))
            .thenThrow(new PaysmartPixException(12345L));

        assertThrows(PaysmartPixException.class, () -> useCase.execute(id));

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveChamarAPaysmartQuandoPixJaEstiverPago() {
        UUID id = UUID.randomUUID();
        MakePix transference = new MakePix(
            id, 12345L, "123", "12", "12345",
            PaymentsStatus.PAID, "CORRENTE", "TESTE", 1000000L, Instant.now()
        );

        when(repository.findById(id)).thenReturn(transference);

        useCase.execute(id);

        verify(paysmartGateway, never()).transferPix(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(repository, never()).save(any());
    }
}
