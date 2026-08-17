package br.com.deltaglobalbank.domain.listInternal

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference.ListInternalTransferenceUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class ListInternalTransferenceUseCaseTest {

    private val repository = mock<InternalTransferenceRepository>()
    private val useCase = ListInternalTransferenceUseCase(repository)

    @Test
    fun `deve retornar apenas waiting quando filtro nao informado`() {
        val transference = InternalTransference(
            id = UUID.randomUUID(),
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.WAITING,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findByStatus(eq(PaymentsStatus.WAITING), any())).thenReturn(page)

        val response = useCase.execute(0, 10, "waiting")

        assertEquals(1, response.content.size)
        assertEquals("WAITING", response.content[0].status)
    }

    @Test
    fun `deve retornar approved quando filtro for approved`() {
        val transference = InternalTransference(
            id = UUID.randomUUID(),
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.APPROVED,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findByStatus(eq(PaymentsStatus.APPROVED), any())).thenReturn(page)

        val response = useCase.execute(0, 10, "approved")

        assertEquals(1, response.content.size)
        assertEquals("APPROVED", response.content[0].status)
    }

    @Test
    fun `deve retornar denied quando filtro for denied`() {
        val transference = InternalTransference(
            id = UUID.randomUUID(),
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.DENIED,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findByStatus(eq(PaymentsStatus.DENIED), any())).thenReturn(page)

        val response = useCase.execute(0, 10, "denied")

        assertEquals(1, response.content.size)
        assertEquals("DENIED", response.content[0].status)
    }

    @Test
    fun `deve retornar todos quando filtro for all`() {
        val transference = InternalTransference(
            id = UUID.randomUUID(),
            accountNumber = 123456L,
            payerId = 1L,
            amount = 10050,
            status = PaymentsStatus.APPROVED,
            description = "Teste",
            requestedAt = Instant.now(),
            requestedById = UUID.randomUUID()
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findAll(any())).thenReturn(page)

        val response = useCase.execute(0, 10, "all")

        assertEquals(1, response.content.size)
        assertEquals("APPROVED", response.content[0].status)
    }
}