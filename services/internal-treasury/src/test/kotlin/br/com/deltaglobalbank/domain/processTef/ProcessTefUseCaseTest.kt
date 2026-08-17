package br.com.deltaglobalbank.domain.processTef

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartGateway
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartTransferException
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.TefNotFoundException
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.features.processTef.ProcessTefUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class ProcessTefUseCaseTest {

    private val repository = mock<InternalTransferenceRepository>()
    private val paysmartGateway = mock<PaysmartGateway>()
    private val useCase = ProcessTefUseCase(repository, paysmartGateway)

    @Test
    fun `deve processar pagamento com sucesso`() {
        val id = UUID.randomUUID()
        val transference = InternalTransference(
            id = id,
            accountNumber = 24145421,
            payerId = 1,
            amount = 4210,
            status = PaymentsStatus.APPROVED,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        whenever(repository.findById(id)).thenReturn(transference)
        whenever(paysmartGateway.transfer(any(), any(), any(), any())).thenReturn(1234)

        useCase.execute(id)

        verify(repository).save(transference)
        assertEquals(PaymentsStatus.PAID, transference.status)
    }

    @Test
    fun `deve lancar excecao quando TEF nao for encontrado`() {
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(null)

        assertThrows<TefNotFoundException> {
            useCase.execute(id)
        }
    }

    @Test
    fun `nao deve salvar quando a Paysmart falhar`() {
        val id = UUID.randomUUID()
        val transferencia = InternalTransference(
            id = id,
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.WAITING,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        whenever(repository.findById(id)).thenReturn(transferencia)
        whenever(paysmartGateway.transfer(any(), any(), any(), any())).thenThrow(PaysmartTransferException::class.java)

        assertThrows<PaysmartTransferException> {
            useCase.execute(id)
        }

        verify(repository, never()).save(any())
    }

    @Test
    fun `nao deve chamar a Paysmart quando TEF ja estiver pago`() {
        val id = UUID.randomUUID()
        val transferencia = InternalTransference(
            id = id,
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.PAID,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        whenever(repository.findById(id)).thenReturn(transferencia)

        useCase.execute(id)

        verify(paysmartGateway, never()).transfer(any(), any(), any(), any())
        verify(repository, never()).save(any())
    }
}