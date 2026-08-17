package br.com.deltaglobalbank.identity.infrastructure.security.cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefreshTokenCookieTest {

    private final JwtIssuerProperties jwtIssuerProperties =
        new JwtIssuerProperties("identity-service", "internal", Duration.ofMinutes(15), Duration.ofDays(7));

    private final RefreshTokenCookie cookie = new RefreshTokenCookie(
        new RefreshTokenCookieProperties("refreshToken", null, true, "Strict", "/auth"), jwtIssuerProperties);

    private final HttpServletResponse resp = mock(HttpServletResponse.class);

    @Test
    void shouldSetHttpOnlySecureCookieWithConfiguredAttributes() {
        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        doNothing().when(resp).addHeader(org.mockito.ArgumentMatchers.eq("Set-Cookie"), header.capture());

        cookie.set(resp, "raw");

        String value = header.getValue();
        assertTrue(value.contains("refreshToken=raw"));
        assertTrue(value.contains("HttpOnly"));
        assertTrue(value.contains("Secure"));
        assertTrue(value.contains("SameSite=Strict"));
        assertTrue(value.contains("Path=/auth"));
        assertFalse(value.contains("Max-Age=0"));
    }

    @Test
    void shouldClearCookieWithMaxAgeZero() {
        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        doNothing().when(resp).addHeader(org.mockito.ArgumentMatchers.eq("Set-Cookie"), header.capture());

        cookie.clear(resp);

        assertTrue(header.getValue().contains("Max-Age=0"));
        assertTrue(header.getValue().contains("Path=/auth"));
    }

    @Test
    void shouldReadTokenWhenCookieIsPresentAndReturnNullWhenAbsent() {
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "raw")});
        assertEquals("raw", cookie.read(req));

        when(req.getCookies()).thenReturn(null);
        assertNull(cookie.read(req));

        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("any", "x")});
        assertNull(cookie.read(req));
    }

    @Test
    void shouldHonorCustomSecureSameSiteAndDomainProperties() {
        RefreshTokenCookie custom = new RefreshTokenCookie(
            new RefreshTokenCookieProperties("refreshToken", "delta.com", false, "Lax", "/auth"), jwtIssuerProperties);

        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        doNothing().when(resp).addHeader(org.mockito.ArgumentMatchers.eq("Set-Cookie"), header.capture());
        custom.set(resp, "raw");

        assertFalse(header.getValue().contains("Secure"));
        assertTrue(header.getValue().contains("SameSite=Lax"));
        assertTrue(header.getValue().contains("Domain=delta.com"));
    }
}
