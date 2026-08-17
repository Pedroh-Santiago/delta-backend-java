package br.com.deltaglobalbank.identity.features.auth.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class LoginControllerTest {

    private final LoginUseCase loginUseCase = mock(LoginUseCase.class);
    private final RefreshTokenCookie refreshCookie = mock(RefreshTokenCookie.class);
    private final LoginController controller = new LoginController(loginUseCase, refreshCookie);

    private final LoginRequest loginRequest = new LoginRequest("admin@delta.com", "Password1!");
    private final HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    private final HttpServletResponse httpResponse = mock(HttpServletResponse.class);

    @Test
    void shouldSetRefreshTokenCookieAndReturnBodyWithoutRefreshOnLogin() {
        when(loginUseCase.execute(any(), any()))
            .thenReturn(new LoginResult("access", "refresh", 900, false));

        ResponseEntity<LoginResponse> response = controller.login(loginRequest, httpRequest, httpResponse);

        verify(refreshCookie).set(httpResponse, "refresh");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        LoginResponse body = response.getBody();
        assertEquals("access", body.accessToken());
        assertEquals(900, body.expiresIn());
        assertEquals(false, body.mustChangePassword());
    }

    @Test
    void shouldResolveClientIpFromXForwardedFor() {
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("user-agent");

        ArgumentCaptor<LoginContext> ctx = ArgumentCaptor.forClass(LoginContext.class);
        when(loginUseCase.execute(any(), ctx.capture()))
            .thenReturn(new LoginResult("access", "refresh", 900, false));

        controller.login(loginRequest, httpRequest, httpResponse);

        assertEquals("192.168.1.1", ctx.getValue().ipAddress());
        assertEquals("user-agent", ctx.getValue().userAgent());
    }

    @Test
    void shouldFallBackToRemoteAddrWhenXForwardedForIsAbsent() {
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.5");
        ArgumentCaptor<LoginContext> ctx = ArgumentCaptor.forClass(LoginContext.class);
        when(loginUseCase.execute(any(), ctx.capture()))
            .thenReturn(new LoginResult("A", "R", 900, false));

        controller.login(loginRequest, httpRequest, httpResponse);

        assertEquals("192.168.1.5", ctx.getValue().ipAddress());
    }
}
