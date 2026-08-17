package br.com.deltaglobalbank.domain.make_pix

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey
import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyRepository
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.IdempotencyFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.PrintWriter
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class MakePixIdempotencyTest {
    private val idempotencyRepository = mock<IdempotencyRepository>()
    private val filter = IdempotencyFilter(idempotencyRepository)

    @Test
    fun `deve retornar resposta salva quando chave de idempotencia ja existe para pix`() {
        val key = UUID.randomUUID()
        val savedResponse = """{"id":"123","accountId":1,"operationAmount":10050}"""

        val savedKey = IdempotencyKey(
            key = key,
            response = savedResponse,
            createdAt = Instant.now()
        )

        whenever(idempotencyRepository.findByKey(key)).thenReturn(savedKey)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val filterChain = mock<FilterChain>()
        val writer = mock<PrintWriter>()

        whenever(request.getHeader("Idempotency-Key")).thenReturn(key.toString())
        whenever(response.writer).thenReturn(writer)

        filter.doFilter(request, response, filterChain)

        verify(writer).write(savedResponse)
    }

    @Test
    fun `deve processar pix e salvar chave quando idempotency key nao existe`() {
        val key = UUID.randomUUID()

        whenever(idempotencyRepository.findByKey(key)).thenReturn(null)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val filterChain = mock<FilterChain>()

        whenever(request.getHeader("Idempotency-Key")).thenReturn(key.toString())

        filter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(any(), any())
        verify(idempotencyRepository).save(any())
    }
}