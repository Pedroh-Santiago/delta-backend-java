package br.com.deltaglobalbank.sharedauth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtValidator: JwtValidator,
    private val revocationChecker: TokenRevocationChecker? = null,
    private val sensitiveMethods: List<String> = emptyList(),
    private val failOpenOnRedisError: Boolean = true
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val validated = jwtValidator.validate(token)
            val checker = revocationChecker
            if (checker != null && sensitiveMethods.contains(request.method.uppercase())) {
                val reason = try {
                    checker.checkRevocation(validated.jti, validated.subject, validated.issuedAt)
                } catch (ex: Exception) {
                    log.warn("Redis indisponível na checagem de revogação: {}", ex.message)
                    if (failOpenOnRedisError) null
                    else { writeUnauthorized(response, "invalid_token", "token_revoked"); return }
                }
                if (reason != null) {
                    val msg = when (reason) {
                        RevocationReason.JTI -> "token_revoked"
                        RevocationReason.USER -> "user_tokens_revoked"
                    }
                    writeUnauthorized(response, "invalid_token", msg)
                    return
                }
            }
            val principal = AuthenticatedPrincipal(
                subject = validated.subject,
                tenantId = validated.tenantId,
                principalType = validated.principalType,
                roles = validated.roles,
                modules = validated.modules,
                mustChangePassword = validated.mustChangePassword,
                jti = validated.jti
            )

            SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(principal)
            filterChain.doFilter(request, response)
        } catch (ex: JwtValidationException) {
            log.debug("Validação de JWT falhou: {}", ex.message)
            writeUnauthorized(response, "invalid_token", ex.message)
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ", ignoreCase = true)) return null
        return header.substring(7).trim().takeIf { it.isNotEmpty() }
    }

    private fun writeUnauthorized(response: HttpServletResponse, error: String, message: String?) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val safeMessage = message?.replace("\\", "\\\\")?.replace("\"", "\\\"") ?: "unauthorized"
        response.writer.write("""{"error":"$error","message":"$safeMessage"}""")
    }
}
