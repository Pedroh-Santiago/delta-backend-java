package br.com.deltaglobalbank.identity.features.auth.refresh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException;
import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RefreshTokenControllerTest {

    private final RefreshTokenUseCase useCase = mock(RefreshTokenUseCase.class);
    private final RefreshTokenCookie refreshCookie = mock(RefreshTokenCookie.class);
    private final RefreshTokenController controller = new RefreshTokenController(useCase, refreshCookie);

    private final HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    private final HttpServletResponse httpResponse = mock(HttpServletResponse.class);

    @Test
    void shouldReadTokenFromCookieAndRotateCookieOnRefresh() {
        when(refreshCookie.read(httpRequest)).thenReturn("cookie-token");
        when(useCase.execute(eq("cookie-token"), any()))
            .thenReturn(new RefreshTokenResult("access", "refresh", 900, false));

        ResponseEntity<RefreshTokenResponse> response = controller.refreshToken(null, httpRequest, httpResponse);

        verify(useCase, times(1)).execute(eq("cookie-token"), any());
        verify(refreshCookie, times(1)).set(httpResponse, "refresh");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access", response.getBody().accessToken());
    }

    @Test
    void shouldFallBackToBodyTokenWhenCookieIsAbsent() {
        when(refreshCookie.read(httpRequest)).thenReturn(null);
        when(useCase.execute(eq("body-token"), any()))
            .thenReturn(new RefreshTokenResult("access", "refresh", 900, false));

        controller.refreshToken(new RefreshTokenRequest("body-token"), httpRequest, httpResponse);

        verify(useCase, times(1)).execute(eq("body-token"), any());
    }

    @Test
    void shouldThrowWhenNeitherCookieNorBodyHasAToken() {
        when(refreshCookie.read(httpRequest)).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> controller.refreshToken(null, httpRequest, httpResponse));
        verify(useCase, times(0)).execute(any(), any());
    }
}
