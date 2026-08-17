package br.com.deltaglobalbank.identity.features.auth.refresh;

import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException;
import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class RefreshTokenController {

    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RefreshTokenCookie refreshTokenCookie;

    public RefreshTokenController(RefreshTokenUseCase refreshTokenUseCase, RefreshTokenCookie refreshTokenCookie) {
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        String cookieToken = refreshTokenCookie.read(httpRequest);
        String token = cookieToken != null ? cookieToken : (request != null ? request.refreshToken() : null);
        if (token == null) {
            throw new InvalidRefreshTokenException();
        }

        RefreshTokenContext context = new RefreshTokenContext(
            resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent")
        );

        RefreshTokenResult result = refreshTokenUseCase.execute(token, context);
        refreshTokenCookie.set(httpResponse, result.refreshToken());

        return ResponseEntity.ok(new RefreshTokenResponse(
            result.accessToken(),
            result.expiresIn(),
            result.mustChangePassword()
        ));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null) {
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }
}
