package br.com.deltaglobalbank.internal_treasury.infrastructure.web

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey
import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import java.time.Instant
import java.util.UUID

@Component
class IdempotencyFilter(
    private val idempotencyRepository: IdempotencyRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val idempotencyKey = request.getHeader("Idempotency-Key")

        if (idempotencyKey == null) {
            filterChain.doFilter(request, response)
            return
        }

        val key = UUID.fromString(idempotencyKey)

        val existing = idempotencyRepository.findByKey(key)

        if (existing != null) {
            response.contentType = "application/json"
            response.writer.write(existing.response)
            return
        } else {
            val wrappedResponse = ContentCachingResponseWrapper(response)
            filterChain.doFilter(request, wrappedResponse)

            val responseBody = String(wrappedResponse.contentAsByteArray)

            idempotencyRepository.save(
                IdempotencyKey(
                    key = key,
                    response = responseBody,
                    createdAt = Instant.now()
                )
            )

            wrappedResponse.copyBodyToResponse()
        }
    }
}