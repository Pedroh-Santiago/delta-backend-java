package br.com.deltaglobalbank.domain.processTef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartGateway;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartTransferException;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.features.processTef.ProcessTefUseCase;
import org.junit.jupiter.api.Test;

class ProcessTefUseCaseTest {

    private final InternalTransferenceRepository repository = mock(InternalTransferenceRepository.class);
    private final PaysmartGateway paysmartGateway = mock(PaysmartGateway.class);
    private final ProcessTefUseCase useCase = new ProcessTefUseCase(repository, paysmartGateway);

    @Test
    void deveProcessarPagamentoComSucesso() {
        UUID id = UUID.randomUUID();
        InternalTransference transference = new InternalTransference(
            id, 24145421L, 1L, 4210, PaymentsStatus.APPROVED, "Teste", Instant.now(), UUID.randomUUID()
        );

        when(repository.findById(id)).thenReturn(transference);
        when(paysmartGateway.transfer(anyLong(), anyLong(), anyInt(), anyString())).thenReturn(1234L);

        useCase.execute(id);

        verify(repository).save(transference);
        assertEquals(PaymentsStatus.PAID, transference.status());
    }

    @Test
    void deveLancarExcecaoQuandoTefNaoForEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(null);

        assertThrows(br.com.deltaglobalbank.internal_treasury.domain.internalTransference.TefNotFoundException.class,
            () -> useCase.execute(id));
    }

    @Test
    void naoDeveSalvarQuandoAPaysmartFalhar() {
        UUID id = UUID.randomUUID();
        InternalTransference transferencia = new InternalTransference(
            id, 123456L, 1L, 10050, PaymentsStatus.WAITING, "Teste", Instant.now(), UUID.randomUUID()
        );

        when(repository.findById(id)).thenReturn(transferencia);
        when(paysmartGateway.transfer(anyLong(), anyLong(), anyInt(), anyString()))
            .thenThrow(new PaysmartTransferException(123456L));

        assertThrows(PaysmartTransferException.class, () -> useCase.execute(id));

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveChamarAPaysmartQuandoTefJaEstiverPago() {
        UUID id = UUID.randomUUID();
        InternalTransference transferencia = new InternalTransference(
            id, 123456L, 1L, 10050, PaymentsStatus.PAID, "Teste", Instant.now(), UUID.randomUUID()
        );

        when(repository.findById(id)).thenReturn(transferencia);

        useCase.execute(id);

        verify(paysmartGateway, never()).transfer(anyLong(), anyLong(), anyInt(), anyString());
        verify(repository, never()).save(any());
    }
}
