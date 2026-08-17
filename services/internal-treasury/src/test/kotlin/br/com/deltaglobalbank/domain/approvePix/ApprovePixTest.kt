package br.com.deltaglobalbank.domain.approvePix

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.features.approvePix.ApprovePixRequest
import br.com.deltaglobalbank.internal_treasury.features.approvePix.ApprovePixUseCase
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.PixPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class ApprovePixTest {

    private val repository = mock<MakePixRepository>()
    private val pixPublisher = mock<PixPublisher>()
    private val useCase = ApprovePixUseCase(repository, pixPublisher )

    @Test
    fun `deve aprovar PIX com sucesso`() {
        val id = UUID.randomUUID()
        val transference = MakePix(
            id = id,
            accountId = 123456L,
            recipientInstitutionCode = "1234",
            recipientBranchCode = "Test",
            recipientAccountNumber = "test",
            status = PaymentsStatus.WAITING,
            recipientAccountType = "21453",
            recipientName = "Testes",
            operationAmount = 12345,
            createdAt = Instant.now()
        )
        whenever(repository.findById(id)).thenReturn(transference)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val request = ApprovePixRequest(approved = listOf(id))
        val result = useCase.execute(request)

        assertEquals(1, result.size)
        assertEquals(id, result[0])
    }

    @Test
    fun `deve ignorar PIX que nao existe`() {
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(null)

        val request = ApprovePixRequest(approved = listOf(id))
        val result = useCase.execute(request)

        assertEquals(0, result.size)
    }

    @Test
    fun `deve rejeitar PIX ja aprovado`() {
        val id = UUID.randomUUID()
        val pix = MakePix(
            id = id,
            accountId = 123456L,
            recipientInstitutionCode = "1234",
            recipientBranchCode = "Test",
            recipientAccountNumber = "test",
            status = PaymentsStatus.APPROVED,
            recipientAccountType = "21453",
            recipientName = "Testes",
            operationAmount = 12345,
            createdAt = Instant.now()
        )

        whenever(repository.findById(id)).thenReturn(pix)

        val request = ApprovePixRequest(approved = listOf(id))

        assertThrows<IllegalArgumentException> {
            useCase.execute(request)
        }
    }

    @Test
    fun `deve publicar mensagem na fila ao aprovar PIX`() {
        val id = UUID.randomUUID()
        val pix = MakePix(
            id = id,
            accountId = 123456L,
            recipientInstitutionCode = "1234",
            recipientBranchCode = "Test",
            recipientAccountNumber = "test",
            status = PaymentsStatus.WAITING,
            recipientAccountType = "21453",
            recipientName = "Testes",
            operationAmount = 21345L,
            createdAt = Instant.now()
        )

        whenever(repository.findById(id)).thenReturn(pix)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        val request = ApprovePixRequest(approved = listOf(id))
        useCase.execute(request)

        verify(repository).findById(id)
    }
}
