package br.com.deltaglobalbank.domain.approveInternalTransference

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece.ApproveInternalTransferenceRequest
import br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece.ApproveInternalTransferenceUseCase
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.TefPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.description
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class ApproveInternalTransferenceTest {

    private val repository = mock<InternalTransferenceRepository>()
    private val tefPublisher = mock<TefPublisher>()
    private val useCase = ApproveInternalTransferenceUseCase(repository, tefPublisher )

    @Test
    fun `deve aprovar TEFs com sucesso`() {
        val id = UUID.randomUUID()
        val transference = InternalTransference(
            id = id,
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.WAITING,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        whenever(repository.findById(id)).thenReturn(transference)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val request = ApproveInternalTransferenceRequest(approved = listOf(id))
        val result = useCase.execute(request)

        assertEquals(1, result.size)
        assertEquals(id, result[0])
    }

    @Test
    fun `deve ignorar TEF que nao existe`() {
        val id = UUID.randomUUID()

        whenever(repository.findById(id)).thenReturn(null)

        val request = ApproveInternalTransferenceRequest(approved = listOf(id))
        val result = useCase.execute(request)

        assertEquals(0, result.size)
    }

    @Test
    fun `deve publicar mensagem na fila ao aprovar TEF`(){
        val id = UUID.randomUUID()
        val transference = InternalTransference(
            id = id,
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.WAITING,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        whenever(repository.findById(id)).thenReturn(transference)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val request = ApproveInternalTransferenceRequest(approved = listOf(id))
        useCase.execute(request)

        verify(tefPublisher).publishApproval(id)
    }

    @Test
    fun `deve rejeitar TEF ja aprovada`() {
        val id = UUID.randomUUID()
        val transference = InternalTransference(
            id = id,
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.APPROVED,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        whenever(repository.findById(id)).thenReturn(transference)

        val request = ApproveInternalTransferenceRequest(approved = listOf(id))

        assertThrows<IllegalArgumentException> {
            useCase.execute(request)
        }
    }
}