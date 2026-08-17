package br.com.deltaglobalbank.domain.orderInternalTransference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InsufficientBalanceException;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference.OrderInternalTransferenceRequest;
import br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference.OrderInternalTransferenceResponse;
import br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference.OrderInternalTransferenceUseCase;
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountResponse;
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountUseCase;
import org.junit.jupiter.api.Test;

class OrderInternalTransferenceUseCaseTest {

    private final InternalTransferenceRepository repository = mock(InternalTransferenceRepository.class);
    private final RetrieveAccountUseCase retrieveAccountUseCase = mock(RetrieveAccountUseCase.class);
    private final BalanceService balanceService = mock(BalanceService.class);

    private final OrderInternalTransferenceUseCase useCase = new OrderInternalTransferenceUseCase(
        repository, retrieveAccountUseCase, balanceService
    );

    @Test
    void deveCriarTransferenciaComSucessoQuandoSaldoSuficiente() {
        OrderInternalTransferenceRequest request = new OrderInternalTransferenceRequest(
            "11144477735", "11144477735", 10050, "Teste"
        );

        when(retrieveAccountUseCase.execute(any())).thenReturn(new RetrieveAccountResponse(1L, 123456L));
        when(balanceService.getBalance(1L)).thenReturn(50000L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderInternalTransferenceResponse response = useCase.execute(request, UUID.randomUUID());

        assertNotNull(response);
        assertEquals(10050, response.amount());
    }

    @Test
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        OrderInternalTransferenceRequest request = new OrderInternalTransferenceRequest(
            "11144477735", "11144477735", 10050, "Teste"
        );

        when(retrieveAccountUseCase.execute(any())).thenReturn(new RetrieveAccountResponse(1L, 123456L));
        when(balanceService.getBalance(1L)).thenReturn(5000L);

        assertThrows(InsufficientBalanceException.class, () -> useCase.execute(request, UUID.randomUUID()));
    }

    @Test
    void deveLancarExcecaoQuandoCpfInvalido() {
        OrderInternalTransferenceRequest request = new OrderInternalTransferenceRequest(
            "12345678912", "12345678912", 10050, "Teste"
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request, UUID.randomUUID()));
    }

    @Test
    void deveSalvarValorEmCentavosCorretamente() {
        OrderInternalTransferenceRequest request = new OrderInternalTransferenceRequest(
            "11144477735", "11144477735", 10050, "Teste centavos"
        );

        when(retrieveAccountUseCase.execute(any())).thenReturn(new RetrieveAccountResponse(1L, 123456L));
        when(balanceService.getBalance(1L)).thenReturn(50000L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderInternalTransferenceResponse response = useCase.execute(request, UUID.randomUUID());

        assertEquals(10050, response.amount());
        assertEquals(1L, response.payerId());
        assertEquals(123456L, response.accountNumber());
    }
}
