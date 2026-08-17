package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenResponse;
import feign.Request;
import feign.codec.DecodeException;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private final HeroSegurosAuthClient authClient = mock(HeroSegurosAuthClient.class);
    private final ServiceAuthProperties props = new ServiceAuthProperties(
        Map.of(
            "clt",
            new ConvenioCredentials("password", "353", "secret", "user", "pwd", "")
        )
    );

    private final TokenService service = new TokenService(authClient, props);

    @Test
    void traduzDecodeExceptionDoFeignEmHeroSegurosInvalidResponse() {
        when(authClient.getToken(any())).thenThrow(feignDecodeException());

        assertThrows(HeroSegurosInvalidResponse.class, () -> service.getToken(Convenio.CLT));
    }

    @Test
    void credenciaisNaoConfiguradasParaOConvenioLancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.getToken(Convenio.SIAPE));
    }

    @Test
    void autenticaECacheiaOToken() {
        when(authClient.getToken(any())).thenReturn(heroSegurosTokenResponseFixture());

        String primeiro = service.getToken(Convenio.CLT);
        String segundo = service.getToken(Convenio.CLT);

        assertEquals(primeiro, segundo);
    }

    private DecodeException feignDecodeException() {
        Request request = Request.create(
            Request.HttpMethod.POST, "https://hero/oauth/token", Map.of(), null, StandardCharsets.UTF_8, null
        );
        return new DecodeException(200, "corpo inesperado", request);
    }

    private HeroSegurosTokenResponse heroSegurosTokenResponseFixture() {
        return new HeroSegurosTokenResponse("bearer", 3600, "token-1", "refresh-1");
    }
}
