package br.com.deltaglobalbank.identity.infrastructure.web

import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientSuspendedException
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyNotFoundException
import br.com.deltaglobalbank.identity.domain.apiKey.TenantInactiveForApiKeyException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.CidrAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.InvalidCidrException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistEntryNotFoundException
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistExceptions
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpNotAllowedException
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException
import br.com.deltaglobalbank.identity.domain.tenant.SlugAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.token.CannotRevokeActiveKeyException
import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException
import br.com.deltaglobalbank.identity.domain.token.MissingRefreshTokenException
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenReuseDetectedException
import br.com.deltaglobalbank.identity.domain.token.SigningKeyNotFoundException
import br.com.deltaglobalbank.identity.domain.user.CannotRemoveOwnAdminRoleException
import br.com.deltaglobalbank.identity.domain.user.CurrentPasswordIncorrectException
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException
import br.com.deltaglobalbank.identity.domain.user.InvalidCredentialsException
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException
import br.com.deltaglobalbank.identity.domain.user.NewPasswordSameAsCurrentException
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotActiveException
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException
import br.com.deltaglobalbank.identity.domain.user.UserNotActiveException
import br.com.deltaglobalbank.identity.domain.user.UserNotFound
import br.com.deltaglobalbank.identity.features.users.me.PrincipalNotFoundException
import br.com.deltaglobalbank.identity.features.users.me.UnsupportedPrincipalTypeException
import br.com.deltaglobalbank.identity.infrastructure.web.responses.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(error = "invalid_credentials"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "validation_error", message = message))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ApiErrorResponse> {
        val supported = ex.supportedHttpMethods?.joinToString(", ") ?: "outros"
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                ApiErrorResponse(
                    error = "method_not_allowed",
                    message = "Método ${ex.method} não suportado. Métodos aceitos: $supported"
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = ex.message ?: "invalid_argument"))
    }

    @ExceptionHandler(CurrentPasswordIncorrectException::class)
    fun handleCurrentPasswordIncorrect(ex: CurrentPasswordIncorrectException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(error = "current_password_incorrect"))
    }

    @ExceptionHandler(NewPasswordSameAsCurrentException::class)
    fun handleNewPasswordSameAsCurrent(ex: NewPasswordSameAsCurrentException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    error = "new_password_same_as_current",
                    message = "A nova senha deve ser diferente da atual"
                )
            )
    }

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleInvalidRefreshToken(ex: InvalidRefreshTokenException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(error = "invalid_refresh_token"))
    }

    @ExceptionHandler(RefreshTokenReuseDetectedException::class)
    fun handleRefreshTokenReuse(ex: RefreshTokenReuseDetectedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiErrorResponse(
                error = "refresh_token_reuse_detected",
                message = "Reuso de token detectado. Todas as sessões foram encerradas. Faça login novamente."
            )
        )
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "email_already_exists", message = "Email já cadastrado"))
    }

    @ExceptionHandler(TenantNotFoundException::class)
    fun handleTenantNotFound(ex: TenantNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "tenant_not_found"))
    }

    @ExceptionHandler(TenantInactiveException::class)
    fun handleTenantInactive(ex: TenantInactiveException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "tenant_inactive", message = "Tenant não está ativo"))
    }

    @ExceptionHandler(RoleNotFoundException::class)
    fun handleRoleNotFound(ex: RoleNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiErrorResponse(
                    error = "role_not_found",
                    message = "Role '${ex.roleCode}' não existe"
                )
            )
    }

    @ExceptionHandler(ModuleNotEnabledForTenantException::class)
    fun handleModuleNotEnabled(ex: ModuleNotEnabledForTenantException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiErrorResponse(
                    error = "module_not_enabled_for_tenant",
                    message = "Módulo '${ex.moduleCode}' não está habilitado para este tenant"
                )
            )
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleDeniedAuthorization(ex: AuthorizationDeniedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse(error = "denied"))
    }

    @ExceptionHandler(DuplicateRoleException::class)
    fun handleDuplicateRole(ex: DuplicateRoleException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "duplicate_role"))
    }

    @ExceptionHandler(SlugAlreadyExistsException::class)
    fun handleSlugAlreadyExists(ex: SlugAlreadyExistsException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "slug_already_exists", message = "Slug já está em uso"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.error("Erro não tratado", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse(error = "internal_error"))
    }

    @ExceptionHandler(ApiClientNotFoundException::class)
    fun handleApiClientNotFound(ex: ApiClientNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "api_client_not_found"))
    }

    @ExceptionHandler(ApiClientSuspendedException::class)
    fun handleApiClientSuspended(ex: ApiClientSuspendedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiErrorResponse(
                    error = "api_client_suspended",
                    message = "api_client não está ativo"
                )
            )
    }

    @ExceptionHandler(TenantInactiveForApiKeyException::class)
    fun handleTenantInactiveForApiKey(ex: TenantInactiveForApiKeyException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "tenant_inactive", message = "Tenant não está ativo"))
    }

    @ExceptionHandler(PrincipalNotFoundException::class)
    fun handlePrincipalNotFound(ex: PrincipalNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(error = "invalid_token", message = "Token não é mais válido"))
    }

    @ExceptionHandler(UnsupportedPrincipalTypeException::class)
    fun handleUnsupportedPrincipalType(ex: UnsupportedPrincipalTypeException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse(error = "internal_error"))
    }

    @ExceptionHandler(ModuleNotFoundException::class)
    fun handleModuleNotFound(ex: ModuleNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "module_not_found"))
    }

    @ExceptionHandler(UserNotFound::class)
    fun handleUserNotFound(ex: UserNotFound) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse(error = "user_not_found"))

    @ExceptionHandler(InvalidCidrException::class)
    fun handleInvalidCidrException(ex: InvalidCidrException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "invalid_cidr"))
    }

    @ExceptionHandler(CidrAlreadyExistsException::class)
    fun handleCidrAlreadyExists(ex: CidrAlreadyExistsException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "cidr_already_exists"))
    }

    @ExceptionHandler(IpAllowlistEntryNotFoundException::class)
    fun handleIpAllowlistEntryNotFoundException(ex: IpAllowlistEntryNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "ip_allowlist_entry_not_found"))
    }

    @ExceptionHandler(IpNotAllowedException::class)
    fun handleIpNotAllowed(ex: IpNotAllowedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(error = "ip_not_allowed"))
    }

    @ExceptionHandler(MissingRefreshTokenException::class)
    fun handleMissingRefreshToken(ex: MissingRefreshTokenException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "missing_refresh_token"))

    @ExceptionHandler(CannotRemoveOwnAdminRoleException::class)
    fun handleCannotRemoveOwnAdminRole(ex: CannotRemoveOwnAdminRoleException)
            : ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(error = "cannot_remove_own_admin_role"))

    @ExceptionHandler(ApiKeyNotFoundException::class)
    fun handleApiKeyNotFoundException(ex: ApiKeyNotFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "api_key_not_found"))

    @ExceptionHandler(UserNotActiveException::class, TenantNotActiveException::class)
    fun handleNotActive(ex: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponse(error = "invalid_credentials"))
    @ExceptionHandler(SigningKeyNotFoundException::class)
    fun handleSigningKeyNotFoundException(ex: SigningKeyNotFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "signing_key_not_found"))

    @ExceptionHandler(CannotRevokeActiveKeyException::class)
    fun handleCannotRevokeActiveKeyException(ex: CannotRevokeActiveKeyException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "cannot_revoke_active_key"))
}