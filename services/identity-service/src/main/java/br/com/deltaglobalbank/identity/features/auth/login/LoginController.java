package br.com.deltaglobalbank.identity.features.auth.login;

import br.com.deltaglobalbank.identity.infrastructure.security.cookie.RefreshTokenCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenCookie refreshTokenCookie;

    public LoginController(LoginUseCase loginUseCase, RefreshTokenCookie refreshTokenCookie) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        LoginContext context = new LoginContext(resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        LoginResult result = loginUseCase.execute(request, context);
        refreshTokenCookie.set(httpResponse, result.refreshToken());

        return ResponseEntity.ok(new LoginResponse(
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
