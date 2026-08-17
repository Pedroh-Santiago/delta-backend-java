package br.com.deltaglobalbank.identity.features.tokens.revoketoken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.sharedauth.JwtAuthenticationFilter;
import br.com.deltaglobalbank.sharedauth.JwtValidator;
import br.com.deltaglobalbank.sharedauth.RevocationReason;
import br.com.deltaglobalbank.sharedauth.TokenRevocationChecker;
import br.com.deltaglobalbank.sharedauth.ValidatedJwt;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

    private final JwtValidator jwtValidator = mock(JwtValidator.class);
    private final TokenRevocationChecker tokenRevocationChecker = mock(TokenRevocationChecker.class);

    private JwtAuthenticationFilter filter(boolean failOpen) {
        return new JwtAuthenticationFilter(
            jwtValidator, tokenRevocationChecker, List.of("POST", "PUT", "PATCH", "DELETE"), failOpen);
    }

    private ValidatedJwt aValidJwt() {
        return new ValidatedJwt(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "user", List.of(), List.of(),
            false, Instant.now().plusSeconds(900), Instant.now());
    }

    @Test
    void shouldReturn401WhenJtiIsRevokedOnSensitiveOperation() throws Exception {
        when(jwtValidator.validate(any())).thenReturn(aValidJwt());
        when(tokenRevocationChecker.checkRevocation(any(), any(), any())).thenReturn(RevocationReason.JTI);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");
        req.addHeader("Authorization", "Bearer x");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter(true).doFilter(req, resp, new MockFilterChain());

        assertEquals(401, resp.getStatus());
    }

    @Test
    void shouldPassOnGetWithoutCheckingRevocation() throws Exception {
        when(jwtValidator.validate(any())).thenReturn(aValidJwt());

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
        req.addHeader("Authorization", "Bearer x");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter(true).doFilter(req, resp, new MockFilterChain());

        assertEquals(200, resp.getStatus());

        verify(tokenRevocationChecker, times(0)).checkRevocation(any(), any(), any());
    }

    @Test
    void shouldPassWhenRedisFailsAndFailOpenIsTrue() throws Exception {
        when(jwtValidator.validate(any())).thenReturn(aValidJwt());
        when(tokenRevocationChecker.checkRevocation(any(), any(), any())).thenThrow(new RuntimeException("redis down"));

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");
        req.addHeader("Authorization", "Bearer x");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter(true).doFilter(req, resp, new MockFilterChain());

        assertEquals(200, resp.getStatus());
    }

    @Test
    void shouldReturn401WhenRedisIsDownAndFailClosed() throws Exception {
        when(jwtValidator.validate(any())).thenReturn(aValidJwt());
        when(tokenRevocationChecker.checkRevocation(any(), any(), any())).thenThrow(new RuntimeException("redis down"));

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");
        req.addHeader("Authorization", "Bearer x");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter(false).doFilter(req, resp, new MockFilterChain());

        assertEquals(401, resp.getStatus());
    }
}
