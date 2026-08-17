package br.com.deltaglobalbank.domain.make_pix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.InsufficientBalanceException;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.features.makePix.MakePixRequest;
import br.com.deltaglobalbank.internal_treasury.features.makePix.MakePixResponse;
import br.com.deltaglobalbank.internal_treasury.features.makePix.MakePixUseCase;
import org.junit.jupiter.api.Test;

class MakePixUseCaseTest {

    private final MakePixRepository pixRepository = mock(MakePixRepository.class);
    private final BalanceService balanceService = mock(BalanceService.class);
    private final MakePixUseCase useCase = new MakePixUseCase(pixRepository, balanceService);

    @Test
    void deveCriarPixComSucessoQuandoSaldoSuficiente() {
        MakePixRequest request = new MakePixRequest(
            1L, "12345", "0001", "123456", "CACC", "João", 10050L
        );

        when(balanceService.getBalance(1L)).thenReturn(50000L);
        when(pixRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MakePixResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(10050L, response.operationAmount());
    }

    @Test
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        MakePixRequest request = new MakePixRequest(
            1L, "12345", "0001", "123456", "CACC", "João", 10050L
        );

        when(balanceService.getBalance(1L)).thenReturn(5000L);

        assertThrows(InsufficientBalanceException.class, () -> useCase.execute(request));
    }

    @Test
    void deveLancarExcecaoQuandoOperationAmountForZero() {
        MakePixRequest request = new MakePixRequest(
            1L, "12345", "0001", "123456", "CACC", "João", 0L
        );

        lenient().when(balanceService.getBalance(1L)).thenReturn(50000L);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    void deveLancarExcecaoQuandoRecipientNameForVazio() {
        when(balanceService.getBalance(1L)).thenReturn(50000L);

        MakePixRequest request = new MakePixRequest(
            1L, "12345", "0001", "123456", "CACC", "", 10050L
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
    }

    @Test
    void deveSalvarValorCorretoNoBanco() {
        MakePixRequest request = new MakePixRequest(
            1L, "12345", "0001", "123456", "CACC", "João", 10050L
        );

        when(balanceService.getBalance(1L)).thenReturn(50000L);
        when(pixRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MakePixResponse response = useCase.execute(request);

        assertEquals(1L, response.accountId());
        assertEquals(10050L, response.operationAmount());
    }
}
