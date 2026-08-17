package br.com.deltaglobalbank.domain.idempotency;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyKey;
import br.com.deltaglobalbank.internal_treasury.domain.idempotency.IdempotencyRepository;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.IdempotencyFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class IdempotencyFilterTest {

    @Test
    void deveRetornarRespostaSalvaQuandoChaveJaExiste() throws Exception {
        IdempotencyRepository repository = mock(IdempotencyRepository.class);

        UUID key = UUID.randomUUID();
        IdempotencyKey savedKey = new IdempotencyKey(
            key,
            "{\"id\": \"123\"}",
            Instant.now()
        );

        when(repository.findByKey(key)).thenReturn(savedKey);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Idempotency-Key")).thenReturn(key.toString());

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        IdempotencyFilter filter = new IdempotencyFilter(repository);
        filter.doFilter(request, response, filterChain);

        verify(writer).write("{\"id\": \"123\"}");
    }

    @Test
    void deveProcessarESalvarQuandoChaveNaoExiste() throws Exception {
        IdempotencyRepository repository = mock(IdempotencyRepository.class);
        UUID key = UUID.randomUUID();

        when(repository.findByKey(key)).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Idempotency-Key")).thenReturn(key.toString());

        IdempotencyFilter filter = new IdempotencyFilter(repository);
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(repository).save(any());
    }
}
