package br.com.deltaglobalbank.sharedauth;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticatedPrincipal authenticatedPrincipal;

    public JwtAuthenticationToken(AuthenticatedPrincipal authenticatedPrincipal) {
        super(authenticatedPrincipal.roles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .map(authority -> (org.springframework.security.core.GrantedAuthority) authority)
            .toList());
        this.authenticatedPrincipal = authenticatedPrincipal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public AuthenticatedPrincipal getPrincipal() {
        return authenticatedPrincipal;
    }
}
