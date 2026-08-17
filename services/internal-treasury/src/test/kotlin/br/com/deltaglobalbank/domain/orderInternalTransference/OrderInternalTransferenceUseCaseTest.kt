package br.com.deltaglobalbank.domain.orderInternalTransference

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InsufficientBalanceException
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference.OrderInternalTransferenceRequest
import br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference.OrderInternalTransferenceUseCase
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountResponse
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.Test

class OrderInternalTransferenceUseCaseTest {
    private val repository = mock<InternalTransferenceRepository>()
    private val retrieveAccountUseCase = mock<RetrieveAccountUseCase>()
    private val balanceService = mock<BalanceService>()

    private val useCase = OrderInternalTransferenceUseCase(
        repository, retrieveAccountUseCase, balanceService
    )

    @Test
    fun `deve criar transferencia com sucesso quando saldo suficiente`() {
        val request = OrderInternalTransferenceRequest(
            payerCpf = "11144477735",
            receiverCpf = "11144477735",
            amount = 10050,
            description = "Teste"
        )

        whenever(retrieveAccountUseCase.execute(any())).thenReturn(
            RetrieveAccountResponse(accountId = 1L, accountNumber = 123456L)
        )

        whenever(balanceService.getBalance(1L)).thenReturn(50000L)

        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val response = useCase.execute(request, UUID.randomUUID())

        assertNotNull(response)
        assertEquals(10050, response.amount)
    }

    @Test
    fun `deve lancar excecao quando saldo insuficiente`() {
        val request = OrderInternalTransferenceRequest(
            payerCpf = "11144477735",
            receiverCpf = "11144477735",
            amount = 10050,
            description = "Teste"
        )

        whenever(retrieveAccountUseCase.execute(any())).thenReturn(
            RetrieveAccountResponse(accountId = 1L, accountNumber = 123456L)
        )

        whenever(balanceService.getBalance(1L)).thenReturn(5000L)

        assertThrows<InsufficientBalanceException> {
            useCase.execute(request, UUID.randomUUID())
        }
    }

    @Test
    fun `deve lancar excecao quando cpf invalido`() {
        val request = OrderInternalTransferenceRequest(
            payerCpf = "12345678912",
            receiverCpf = "12345678912",
            amount = 10050,
            description = "Teste"
        )

        assertThrows<IllegalArgumentException> {
            useCase.execute(request, UUID.randomUUID())
        }
    }

    @Test
    fun `deve salvar valor em centavos corretamente`() {
        val request = OrderInternalTransferenceRequest(
            payerCpf = "11144477735",
            receiverCpf = "11144477735",
            amount = 10050,
            description = "Teste centavos"
        )

        whenever(retrieveAccountUseCase.execute(any())).thenReturn(
            RetrieveAccountResponse(accountId = 1L, accountNumber = 123456L)
        )
        whenever(balanceService.getBalance(1L)).thenReturn(50000L)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val response = useCase.execute(request, UUID.randomUUID())

        assertEquals(10050, response.amount)
        assertEquals(1L, response.payerId)
        assertEquals(123456L, response.accountNumber)
    }
}