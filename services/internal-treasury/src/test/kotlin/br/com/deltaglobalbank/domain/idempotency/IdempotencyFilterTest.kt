package br.com.deltaglobalbank.domain.idempotency

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

class IdempotencyFilterTest {
    @Test
    fun `deve retornar resposta salva quando chave ja existe`() {
        val repository = mock<IdempotencyRepository>()

        val key = UUID.randomUUID()
        val savedKey = IdempotencyKey(
            key = key,
            response = """{"id": "123"}""",
            createdAt = Instant.now()
        )

        whenever(repository.findByKey(key)).thenReturn(savedKey)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val filterChain = mock<FilterChain>()

        whenever(request.getHeader("Idempotency-Key")).thenReturn(key.toString())

        val writer = mock<PrintWriter>()
        whenever(response.writer).thenReturn(writer)

        val filter = IdempotencyFilter(repository)
        filter.doFilter(request, response, filterChain)

        verify(writer).write("""{"id": "123"}""")

    }

    @Test
    fun `deve processar e salvar quando chave nao existe`() {
        val repository = mock<IdempotencyRepository>()
        val key = UUID.randomUUID()

        whenever(repository.findByKey(key)).thenReturn(null)

        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val filterChain = mock<FilterChain>()

        whenever(request.getHeader("Idempotency-Key")).thenReturn(key.toString())

        val filter = IdempotencyFilter(repository)
        filter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(any(), any())
        verify(repository).save(any())
    }
}