package br.com.deltaglobalbank.internal_treasury.infrastructure.web;

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey;
import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyRepository idempotencyRepository;

    public IdempotencyFilter(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID key = UUID.fromString(idempotencyKey);

        IdempotencyKey existing = idempotencyRepository.findByKey(key);

        if (existing != null) {
            response.setContentType("application/json");
            response.getWriter().write(existing.response());
            return;
        } else {
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, wrappedResponse);

            String responseBody = new String(wrappedResponse.getContentAsByteArray());

            idempotencyRepository.save(
                new IdempotencyKey(
                    key,
                    responseBody,
                    Instant.now()
                )
            );

            wrappedResponse.copyBodyToResponse();
        }
    }
}
