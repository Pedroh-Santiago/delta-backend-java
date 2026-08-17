package br.com.deltaglobalbank.domain.make_pix

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService
import br.com.deltaglobalbank.internal_treasury.domain.makePix.InsufficientBalanceException
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.features.makePix.MakePixRequest
import br.com.deltaglobalbank.internal_treasury.features.makePix.MakePixUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.Test

class MakePixUseCaseTest {

    private val pixRepository = mock<MakePixRepository>()
    private val balanceService = mock<BalanceService>()
    private val useCase = MakePixUseCase(pixRepository, balanceService)

    @Test
    fun `deve criar pix com sucesso quando saldo suficiente`() {
        val request = MakePixRequest(
            accountId = 1L,
            recipientInstitutionCode = "12345",
            recipientBranchCode = "0001",
            recipientAccountNumber = "123456",
            recipientAccountType = "CACC",
            recipientName = "João",
            operationAmount = 10050
        )

        whenever(balanceService.getBalance(1L)).thenReturn(50000L)
        whenever(pixRepository.save(any())).thenAnswer { it.arguments[0] }

        val response = useCase.execute(request)

        assertNotNull(response)
        assertEquals(10050, response.operationAmount)
    }

    @Test
    fun `deve lancar excecao quando saldo insuficiente`() {
        val request = MakePixRequest(
            accountId = 1L,
            recipientInstitutionCode = "12345",
            recipientBranchCode = "0001",
            recipientAccountNumber = "123456",
            recipientAccountType = "CACC",
            recipientName = "João",
            operationAmount = 10050
        )

        whenever(balanceService.getBalance(1L)).thenReturn(5000L)

        assertThrows<InsufficientBalanceException> {
            useCase.execute(request)
        }
    }

    @Test
    fun `deve lancar excecao quando operationAmount for zero`() {
        val request = MakePixRequest(
            accountId = 1L,
            recipientInstitutionCode = "12345",
            recipientBranchCode = "0001",
            recipientAccountNumber = "123456",
            recipientAccountType = "CACC",
            recipientName = "João",
            operationAmount = 0
        )

        assertThrows<IllegalArgumentException> {
            useCase.execute(request)
        }
    }

    @Test
    fun `deve lancar excecao quando recipientName for vazio`() {
        whenever(balanceService.getBalance(1L)).thenReturn(50000L)

        val request = MakePixRequest(
            accountId = 1L,
            recipientInstitutionCode = "12345",
            recipientBranchCode = "0001",
            recipientAccountNumber = "123456",
            recipientAccountType = "CACC",
            recipientName = "",
            operationAmount = 10050
        )

        assertThrows<IllegalArgumentException> {
            useCase.execute(request)
        }
    }

    @Test
    fun `deve salvar valor correto no banco`() {
        val request = MakePixRequest(
            accountId = 1L,
            recipientInstitutionCode = "12345",
            recipientBranchCode = "0001",
            recipientAccountNumber = "123456",
            recipientAccountType = "CACC",
            recipientName = "João",
            operationAmount = 10050
        )

        whenever(balanceService.getBalance(1L)).thenReturn(50000L)
        whenever(pixRepository.save(any())).thenAnswer { it.arguments[0] }

        val response = useCase.execute(request)

        assertEquals(1L, response.accountId)
        assertEquals(10050, response.operationAmount)
    }
}