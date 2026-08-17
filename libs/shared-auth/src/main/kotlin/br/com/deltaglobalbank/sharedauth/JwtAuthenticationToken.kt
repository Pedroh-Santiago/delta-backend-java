package br.com.deltaglobalbank.sharedauth

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtAuthenticationToken(
    private val authenticatedPrincipal: AuthenticatedPrincipal
) : AbstractAuthenticationToken(
    authenticatedPrincipal.roles.map { SimpleGrantedAuthority("ROLE_$it") }
) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null
    override fun getPrincipal(): AuthenticatedPrincipal = authenticatedPrincipal
}
