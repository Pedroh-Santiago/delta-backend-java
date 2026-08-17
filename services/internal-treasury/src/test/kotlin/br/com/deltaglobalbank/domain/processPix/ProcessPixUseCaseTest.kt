package br.com.deltaglobalbank.domain.processPix

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PaysmartPixException
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixGateway
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixNotFoundException
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.features.processPix.ProcessPixUseCase
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class ProcessPixUseCaseTest {

    private val repository = mock<MakePixRepository>()
    private val paysmartGateway = mock<PixGateway>()
    private val useCase = ProcessPixUseCase(repository, paysmartGateway)

    @Test
    fun `deve processar pagamento com sucesso`() {
        val id = UUID.randomUUID()
        val transference = MakePix(
            id = id,
            accountId = 12345,
            recipientInstitutionCode = "123",
            recipientBranchCode = "12",
            recipientAccountNumber = "12345",
            status = PaymentsStatus.APPROVED,
            recipientAccountType = "CORRENTE",
            recipientName = "TESTE",
            operationAmount = 1000000,
            createdAt = Instant.now()
        )

        whenever(repository.findById(id)).thenReturn(transference)
        whenever(paysmartGateway.transferPix(any(), any(), any(), any(), any(), any(), any())).thenReturn(1234)

        useCase.execute(id)

        verify(repository).save(transference)
        assertEquals(PaymentsStatus.PAID, transference.status)
    }

    @Test
    fun `deve lancar excecao quando PIX nao for encontrado`() {
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(null)

        assertThrows<PixNotFoundException> {
            useCase.execute(id)
        }
    }

    @Test
    fun `nao deve salvar quando paysmart falhar`() {
        val id = UUID.randomUUID()
        val transference = MakePix(
            id = id,
            accountId = 12345,
            recipientInstitutionCode = "123",
            recipientBranchCode = "12",
            recipientAccountNumber = "12345",
            status = PaymentsStatus.WAITING,
            recipientAccountType = "CORRENTE",
            recipientName = "TESTE",
            operationAmount = 1000000,
            createdAt = Instant.now()
        )

        whenever(repository.findById(id)).thenReturn(transference)
        whenever(paysmartGateway.transferPix(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any())).thenThrow(PaysmartPixException(12345))

        assertThrows<PaysmartPixException> {
            useCase.execute(id)
        }

        verify(repository, never()).save(any())
    }

    @Test
    fun `nao deve chamar a Paysmart quando PIX ja estiver pago`() {
        val id = UUID.randomUUID()
        val transference = MakePix(
            id = id,
            accountId = 12345,
            recipientInstitutionCode = "123",
            recipientBranchCode = "12",
            recipientAccountNumber = "12345",
            status = PaymentsStatus.PAID,
            recipientAccountType = "CORRENTE",
            recipientName = "TESTE",
            operationAmount = 1000000,
            createdAt = Instant.now()
        )

        whenever(repository.findById(id)).thenReturn(transference)

        useCase.execute(id)

        verify(paysmartGateway, never()).transferPix(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
            )
        verify(repository, never()).save(any())
    }
}