package br.com.deltaglobalbank.domain.listPix

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import br.com.deltaglobalbank.internal_treasury.features.listPix.ListPixUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class ListMakePixUseCaseTest {
    private val repository = mock<MakePixRepository>()
    private val useCase = ListPixUseCase(repository)

    @Test
    fun `deve retornar apenas waiting quando filtro nao informado`() {
        val transference = MakePix(
            id = UUID.randomUUID(),
            accountId = 123456L,
            recipientInstitutionCode = "12",
            operationAmount = 10050,
            status = PaymentsStatus.WAITING,
            recipientName = "Teste",
            createdAt = Instant.now(),
            recipientBranchCode = "1234",
            recipientAccountNumber = "214421",
            recipientAccountType = "214214"
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findByStatus(eq(PaymentsStatus.WAITING), any())).thenReturn(page)

        val response = useCase.execute(0, 10, "waiting")

        assertEquals(1, response.content.size)
        assertEquals("WAITING", response.content[0].status)
    }

    @Test
    fun `deve retornar approved quando filtro for approved`() {
        val transference = MakePix(
            id = UUID.randomUUID(),
            accountId = 123456L,
            recipientInstitutionCode = "12",
            operationAmount = 10050,
            status = PaymentsStatus.APPROVED,
            recipientName = "Teste",
            createdAt = Instant.now(),
            recipientBranchCode = "1234",
            recipientAccountNumber = "214421",
            recipientAccountType = "214214"
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findByStatus(eq(PaymentsStatus.APPROVED), any())).thenReturn(page)

        val response = useCase.execute(0, 10, "approved")

        assertEquals(1, response.content.size)
        assertEquals("APPROVED", response.content[0].status)
    }

    @Test
    fun `deve retornar denied quando filtro for denied`() {
        val transference = MakePix(
            id = UUID.randomUUID(),
            accountId = 123456L,
            recipientInstitutionCode = "12",
            operationAmount = 10050,
            status = PaymentsStatus.DENIED,
            recipientName = "Teste",
            createdAt = Instant.now(),
            recipientBranchCode = "1234",
            recipientAccountNumber = "214421",
            recipientAccountType = "214214"
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findByStatus(eq(PaymentsStatus.DENIED), any())).thenReturn(page)

        val response = useCase.execute(0, 10, "denied")

        assertEquals(1, response.content.size)
        assertEquals("DENIED", response.content[0].status)
    }

    @Test
    fun `deve retornar todos quando filtro for all`() {
        val transference = MakePix(
            id = UUID.randomUUID(),
            accountId = 123456L,
            recipientInstitutionCode = "12",
            operationAmount = 10050,
            status = PaymentsStatus.WAITING,
            recipientName = "Teste",
            createdAt = Instant.now(),
            recipientBranchCode = "1234",
            recipientAccountNumber = "214421",
            recipientAccountType = "214214"
        )

        val page = PageImpl(listOf(transference))
        whenever(repository.findAll(any())).thenReturn(page)

        val response = useCase.execute(0, 10, "all")

        assertEquals(1, response.content.size)
        assertEquals("WAITING", response.content[0].status)
    }
}