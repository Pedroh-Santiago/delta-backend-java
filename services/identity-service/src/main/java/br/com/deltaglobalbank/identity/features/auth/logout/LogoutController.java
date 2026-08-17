package br.com.deltaglobalbank.identity.features.auth.logout;

import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LogoutController {

    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenCookie refreshTokenCookie;

    public LogoutController(LogoutUseCase logoutUseCase, RefreshTokenCookie refreshTokenCookie) {
        this.logoutUseCase = logoutUseCase;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @RequestParam(defaultValue = "false") boolean allSessions,
        @RequestBody(required = false) LogoutRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        String cookieToken = refreshTokenCookie.read(httpRequest);
        String token = cookieToken != null ? cookieToken : (request != null ? request.refreshToken() : null);

        logoutUseCase.logout(principal.subject(), principal.jti(), token, allSessions);
        refreshTokenCookie.clear(httpResponse);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
