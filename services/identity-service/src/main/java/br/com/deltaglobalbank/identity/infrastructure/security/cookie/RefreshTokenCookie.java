package br.com.deltaglobalbank.identity.infrastructure.security.cookie;

import java.time.Duration;

import br.com.deltaglobalbank.identity.infrastructure.security.jwt.JwtIssuerProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookie {

    private final RefreshTokenCookieProperties props;
    private final JwtIssuerProperties jwtProps;

    public RefreshTokenCookie(RefreshTokenCookieProperties props, JwtIssuerProperties jwtProps) {
        this.props = props;
        this.jwtProps = jwtProps;
    }

    public void set(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, jwtProps.refreshTokenTtl()).toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(props.name())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(props.name(), value)
            .httpOnly(true)
            .secure(props.secure())
            .sameSite(props.sameSite())
            .path(props.path())
            .maxAge(maxAge);
        if (props.domain() != null) {
            b.domain(props.domain());
        }
        return b.build();
    }
}
