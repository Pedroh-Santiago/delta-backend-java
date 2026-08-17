package br.com.deltaglobalbank.sharedauth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtValidator jwtValidator;
    private final TokenRevocationChecker revocationChecker;
    private final List<String> sensitiveMethods;
    private final boolean failOpenOnRedisError;

    public JwtAuthenticationFilter(
        JwtValidator jwtValidator,
        TokenRevocationChecker revocationChecker,
        List<String> sensitiveMethods,
        boolean failOpenOnRedisError
    ) {
        this.jwtValidator = jwtValidator;
        this.revocationChecker = revocationChecker;
        this.sensitiveMethods = sensitiveMethods;
        this.failOpenOnRedisError = failOpenOnRedisError;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            ValidatedJwt validated = jwtValidator.validate(token);
            if (revocationChecker != null
                && sensitiveMethods.contains(request.getMethod().toUpperCase())) {
                RevocationReason reason;
                try {
                    reason = revocationChecker.checkRevocation(
                        validated.jti(), validated.subject(), validated.issuedAt());
                } catch (Exception ex) {
                    log.warn("Redis indisponível na checagem de revogação: {}", ex.getMessage());
                    if (failOpenOnRedisError) {
                        reason = null;
                    } else {
                        writeUnauthorized(response, "invalid_token", "token_revoked");
                        return;
                    }
                }
                if (reason != null) {
                    String msg = switch (reason) {
                        case JTI -> "token_revoked";
                        case USER -> "user_tokens_revoked";
                    };
                    writeUnauthorized(response, "invalid_token", msg);
                    return;
                }
            }
            AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                validated.subject(),
                validated.tenantId(),
                validated.principalType(),
                validated.roles(),
                validated.modules(),
                validated.mustChangePassword(),
                validated.jti()
            );

            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(principal));
            filterChain.doFilter(request, response);
        } catch (JwtValidationException ex) {
            log.debug("Validação de JWT falhou: {}", ex.getMessage());
            writeUnauthorized(response, "invalid_token", ex.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String value = header.substring(7).trim();
        return value.isEmpty() ? null : value;
    }

    private void writeUnauthorized(HttpServletResponse response, String error, String message)
        throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String safeMessage = message != null
            ? message.replace("\\", "\\\\").replace("\"", "\\\"")
            : "unauthorized";
        response.getWriter().write("{\"error\":\"" + error + "\",\"message\":\"" + safeMessage + "\"}");
    }
}
