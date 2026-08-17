package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAuthenticationFailed;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksNotConfigured;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksLoginResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

class SWorksTokenServiceTest {

    private final SWorksAuthClient authClient = mock(SWorksAuthClient.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-08-04T12:00:00Z"));

    private SWorksTokenService service() {
        return service("sworks-user", "sworks-pwd");
    }

    private SWorksTokenService service(String username, String password) {
        return new SWorksTokenService(authClient, properties(username, password), clock);
    }

    @Test
    void autenticaNaPrimeiraChamada() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1"));

        assertEquals("token-1", service().getToken());

        verify(authClient, times(1)).login(any());
    }

    @Test
    void enviaUsernamePasswordEGrantTypeNoFormulario() {
        org.mockito.ArgumentCaptor<MultiValueMap<String, String>> form = org.mockito.ArgumentCaptor.forClass(MultiValueMap.class);
        when(authClient.login(form.capture())).thenReturn(loginResponse("token-1"));

        service().getToken();

        assertEquals("sworks-user", form.getValue().getFirst("username"));
        assertEquals("sworks-pwd", form.getValue().getFirst("password"));
        assertEquals("password", form.getValue().getFirst("grant_type"));
    }

    @Test
    void reusaOTokenEmCacheSemBaterNaRedeDeNovo() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1"));
        SWorksTokenService service = service();

        String primeiro = service.getToken();
        String segundo = service.getToken();

        assertEquals(primeiro, segundo);
        verify(authClient, times(1)).login(any());
    }

    @Test
    void mantemOTokenEmCacheUmSegundoAntesDaMargemDeSeguranca() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1", 3600));
        SWorksTokenService service = service();

        service.getToken();
        clock.advanceSeconds(3569);
        service.getToken();

        verify(authClient, times(1)).login(any());
    }

    @Test
    void reAutenticaQuandoOTokenExpiraDentroDaMargem() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1"), loginResponse("token-2"));
        SWorksTokenService service = service();

        assertEquals("token-1", service.getToken());
        clock.advanceSeconds(3570);

        assertEquals("token-2", service.getToken());
        verify(authClient, times(2)).login(any());
    }

    @Test
    void invalidateForcaNovaAutenticacao() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1"), loginResponse("token-2"));
        SWorksTokenService service = service();

        assertEquals("token-1", service.getToken());
        service.invalidate();

        assertEquals("token-2", service.getToken());
        verify(authClient, times(2)).login(any());
    }

    @Test
    void tokenCurtoDe20sComoODaHomologacaoEReusadoPor15s() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1", 20), loginResponse("token-2", 20));
        SWorksTokenService service = service();

        assertEquals("token-1", service.getToken());
        clock.advanceSeconds(14);
        assertEquals("token-1", service.getToken());
        verify(authClient, times(1)).login(any());

        clock.advanceSeconds(1);
        assertEquals("token-2", service.getToken());
        verify(authClient, times(2)).login(any());
    }

    @Test
    void naoDeixaOCacheNascerVencidoQuandoExpiresInEMenorQueAMargem() {
        when(authClient.login(any())).thenReturn(loginResponse("token-1", 10));
        SWorksTokenService service = service();

        service.getToken();
        service.getToken();

        verify(authClient, times(1)).login(any());
    }

    @Test
    void credencialInvalidaChegaComo500EViraFalhaDeAutenticacaoSemRetry() {
        String problemDetails = "{\"status\":500,\"detail\":\"Login Attempt Failed\",\"traceId\":\"00-7f4c2d54-00\"}";
        when(authClient.login(any())).thenThrow(feignError(500, problemDetails));

        SWorksAuthenticationFailed erro = assertThrows(SWorksAuthenticationFailed.class, () -> service().getToken());

        assertEquals(500, erro.getStatus());
        assertTrue(erro.getDetail().contains("Login Attempt Failed"));
        assertTrue(erro.getMessage().contains("traceId"), "o traceId do SWorks precisa chegar no log");
        verify(authClient, times(1)).login(any());
    }

    @Test
    void sworksForaDeAlcanceNoLoginEIndisponibilidadeENaoFalhaDeCredencial() {
        when(authClient.login(any())).thenThrow(
            new RetryableException(-1, "connection reset", Request.HttpMethod.POST, (Long) null, requisicaoDeToken())
        );

        SWorksUnavailable erro = assertThrows(SWorksUnavailable.class, () -> service().getToken());

        assertEquals(0, erro.getStatus());
    }

    @Test
    void quatrocentosEUmNoLoginTambemViraFalhaDeAutenticacao() {
        when(authClient.login(any())).thenThrow(feignError(401, null));

        assertEquals(401, assertThrows(SWorksAuthenticationFailed.class, () -> service().getToken()).getStatus());
    }

    @Test
    void naoChamaOClientQuandoAsCredenciaisNaoEstaoConfiguradas() {
        assertThrows(SWorksNotConfigured.class, () -> service("", "sworks-pwd").getToken());
        assertThrows(SWorksNotConfigured.class, () -> service("sworks-user", "").getToken());

        verify(authClient, org.mockito.Mockito.never()).login(any());
    }

    private SWorksProperties properties(String username, String password) {
        return new SWorksProperties(
            "https://sworkshml-delta.simply.com.br/SWorks.WebApi",
            new SWorksCredentials(username, password, "password"),
            true,
            null,
            null
        );
    }

    private SWorksLoginResponse loginResponse(String token) {
        return loginResponse(token, 3600);
    }

    private SWorksLoginResponse loginResponse(String token, long expiresIn) {
        return new SWorksLoginResponse(token, "bearer", expiresIn);
    }

    private Request requisicaoDeToken() {
        return Request.create(
            Request.HttpMethod.POST,
            "https://sworkshml-delta.simply.com.br/SWorks.WebApi/token",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
    }

    private FeignException feignError(int status, String body) {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "https://sworkshml-delta.simply.com.br/SWorks.WebApi/token",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response.Builder builder = Response.builder()
            .status(status)
            .reason("erro")
            .request(request)
            .headers(Map.of());
        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return FeignException.errorStatus("SWorksAuthClient#login(MultiValueMap)", builder.build());
    }

    private static final class MutableClock extends Clock {
        private Instant current;

        private MutableClock(Instant current) {
            this.current = current;
        }

        @Override
        public Instant instant() {
            return current;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        void advanceSeconds(long seconds) {
            current = current.plusSeconds(seconds);
        }
    }
}
