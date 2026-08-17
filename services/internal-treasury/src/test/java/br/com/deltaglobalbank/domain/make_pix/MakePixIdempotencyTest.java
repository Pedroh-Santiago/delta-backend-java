package br.com.deltaglobalbank.domain.make_pix;

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

class MakePixIdempotencyTest {

    private final IdempotencyRepository idempotencyRepository = mock(IdempotencyRepository.class);
    private final IdempotencyFilter filter = new IdempotencyFilter(idempotencyRepository);

    @Test
    void deveRetornarRespostaSalvaQuandoChaveDeIdempotenciaJaExisteParaPix() throws Exception {
        UUID key = UUID.randomUUID();
        String savedResponse = "{\"id\":\"123\",\"accountId\":1,\"operationAmount\":10050}";

        IdempotencyKey savedKey = new IdempotencyKey(key, savedResponse, Instant.now());

        when(idempotencyRepository.findByKey(key)).thenReturn(savedKey);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);

        when(request.getHeader("Idempotency-Key")).thenReturn(key.toString());
        when(response.getWriter()).thenReturn(writer);

        filter.doFilter(request, response, filterChain);

        verify(writer).write(savedResponse);
    }

    @Test
    void deveProcessarPixESalvarChaveQuandoIdempotencyKeyNaoExiste() throws Exception {
        UUID key = UUID.randomUUID();

        when(idempotencyRepository.findByKey(key)).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Idempotency-Key")).thenReturn(key.toString());

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(idempotencyRepository).save(any());
    }
}
