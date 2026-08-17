package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksTokenExpired;
import org.junit.jupiter.api.Test;

class SWorksBearerExecutorTest {

    private final SWorksTokenService tokenService = mock(SWorksTokenService.class);
    private final SWorksBearerExecutor executor = new SWorksBearerExecutor(tokenService);

    @Test
    void passaOTokenNoFormatoBearer() {
        when(tokenService.getToken()).thenReturn("token-1");

        String recebido = executor.execute(authorization -> authorization);

        assertEquals("Bearer token-1", recebido);
    }

    @Test
    void em401InvalidaOCacheERepeteUmaVezComOTokenNovo() {
        when(tokenService.getToken()).thenReturn("token-velho", "token-novo");
        List<String> usados = new ArrayList<>();

        String recebido = executor.execute(authorization -> {
            usados.add(authorization);
            if (usados.size() == 1) {
                throw new SWorksTokenExpired();
            }
            return authorization;
        });

        assertEquals("Bearer token-novo", recebido);
        assertEquals(List.of("Bearer token-velho", "Bearer token-novo"), usados);
        verify(tokenService, times(1)).invalidate();
    }

    @Test
    void naoTentaUmaTerceiraVezQuandoOTokenNovoTambemERecusado() {
        when(tokenService.getToken()).thenReturn("token-velho", "token-novo");
        int[] tentativas = {0};

        assertThrows(SWorksTokenExpired.class, () -> executor.<Void>execute(authorization -> {
            tentativas[0]++;
            throw new SWorksTokenExpired();
        }));

        assertEquals(2, tentativas[0]);
        verify(tokenService, times(1)).invalidate();
    }
}
