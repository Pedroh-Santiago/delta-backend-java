package br.com.deltaglobalbank.identity.infrastructure.web;

import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientNotFoundException;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiClientSuspendedException;
import br.com.deltaglobalbank.identity.domain.apiKey.ApiKeyNotFoundException;
import br.com.deltaglobalbank.identity.domain.apiKey.TenantInactiveForApiKeyException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.CidrAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.InvalidCidrException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpAllowlistEntryNotFoundException;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.IpNotAllowedException;
import br.com.deltaglobalbank.identity.domain.module.ModuleNotFoundException;
import br.com.deltaglobalbank.identity.domain.tenant.SlugAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.token.CannotRevokeActiveKeyException;
import br.com.deltaglobalbank.identity.domain.token.InvalidRefreshTokenException;
import br.com.deltaglobalbank.identity.domain.token.MissingRefreshTokenException;
import br.com.deltaglobalbank.identity.domain.token.RefreshTokenReuseDetectedException;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.CannotRemoveOwnAdminRoleException;
import br.com.deltaglobalbank.identity.domain.user.CannotResetOwnPasswordException;
import br.com.deltaglobalbank.identity.domain.user.CurrentPasswordIncorrectException;
import br.com.deltaglobalbank.identity.domain.user.DuplicateRoleException;
import br.com.deltaglobalbank.identity.domain.user.EmailAlreadyExistsException;
import br.com.deltaglobalbank.identity.domain.user.InvalidCredentialsException;
import br.com.deltaglobalbank.identity.domain.user.ModuleNotEnabledForTenantException;
import br.com.deltaglobalbank.identity.domain.user.NewPasswordSameAsCurrentException;
import br.com.deltaglobalbank.identity.domain.user.RoleNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.TenantInactiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotActiveException;
import br.com.deltaglobalbank.identity.domain.user.TenantNotFoundException;
import br.com.deltaglobalbank.identity.domain.user.UserLockedException;
import br.com.deltaglobalbank.identity.domain.user.UserNotActiveException;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.features.users.me.PrincipalNotFoundException;
import br.com.deltaglobalbank.identity.features.users.me.UnsupportedPrincipalTypeException;
import br.com.deltaglobalbank.identity.infrastructure.web.responses.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse("invalid_credentials"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(it -> it.getField() + ": " + it.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse("validation_error", message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedHttpMethods() != null
            ? ex.getSupportedHttpMethods().stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("outros")
            : "outros";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ApiErrorResponse(
            "method_not_allowed",
            "Método " + ex.getMethod() + " não suportado. Métodos aceitos: " + supported
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(ex.getMessage() != null ? ex.getMessage() : "invalid_argument"));
    }

    @ExceptionHandler(CurrentPasswordIncorrectException.class)
    public ResponseEntity<ApiErrorResponse> handleCurrentPasswordIncorrect(CurrentPasswordIncorrectException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse("current_password_incorrect"));
    }

    @ExceptionHandler(NewPasswordSameAsCurrentException.class)
    public ResponseEntity<ApiErrorResponse> handleNewPasswordSameAsCurrent(NewPasswordSameAsCurrentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
            "new_password_same_as_current", "A nova senha deve ser diferente da atual"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse("invalid_refresh_token"));
    }

    @ExceptionHandler(RefreshTokenReuseDetectedException.class)
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenReuse(RefreshTokenReuseDetectedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(
            "refresh_token_reuse_detected",
            "Reuso de token detectado. Todas as sessões foram encerradas. Faça login novamente."));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("email_already_exists", "Email já cadastrado"));
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantNotFound(TenantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("tenant_not_found"));
    }

    @ExceptionHandler(TenantInactiveException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantInactive(TenantInactiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("tenant_inactive", "Tenant não está ativo"));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFound(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("role_not_found", "Role '" + ex.getRoleCode() + "' não existe"));
    }

    @ExceptionHandler(ModuleNotEnabledForTenantException.class)
    public ResponseEntity<ApiErrorResponse> handleModuleNotEnabled(ModuleNotEnabledForTenantException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
            "module_not_enabled_for_tenant",
            "Módulo '" + ex.getModuleCode() + "' não está habilitado para este tenant"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleDeniedAuthorization(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse("denied"));
    }

    @ExceptionHandler(DuplicateRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRole(DuplicateRoleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse("duplicate_role"));
    }

    @ExceptionHandler(SlugAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleSlugAlreadyExists(SlugAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("slug_already_exists", "Slug já está em uso"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse("internal_error"));
    }

    @ExceptionHandler(ApiClientNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleApiClientNotFound(ApiClientNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("api_client_not_found"));
    }

    @ExceptionHandler(ApiClientSuspendedException.class)
    public ResponseEntity<ApiErrorResponse> handleApiClientSuspended(ApiClientSuspendedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("api_client_suspended", "api_client não está ativo"));
    }

    @ExceptionHandler(TenantInactiveForApiKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantInactiveForApiKey(TenantInactiveForApiKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("tenant_inactive", "Tenant não está ativo"));
    }

    @ExceptionHandler(PrincipalNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePrincipalNotFound(PrincipalNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiErrorResponse("invalid_token", "Token não é mais válido"));
    }

    @ExceptionHandler(UnsupportedPrincipalTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedPrincipalType(UnsupportedPrincipalTypeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse("internal_error"));
    }

    @ExceptionHandler(ModuleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleModuleNotFound(ModuleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("module_not_found"));
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("user_not_found"));
    }

    @ExceptionHandler(InvalidCidrException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCidrException(InvalidCidrException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse("invalid_cidr"));
    }

    @ExceptionHandler(CidrAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCidrAlreadyExists(CidrAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("cidr_already_exists"));
    }

    @ExceptionHandler(IpAllowlistEntryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIpAllowlistEntryNotFoundException(IpAllowlistEntryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("ip_allowlist_entry_not_found"));
    }

    @ExceptionHandler(IpNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleIpNotAllowed(IpNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse("ip_not_allowed"));
    }

    @ExceptionHandler(MissingRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRefreshToken(MissingRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse("missing_refresh_token"));
    }

    @ExceptionHandler(CannotRemoveOwnAdminRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleCannotRemoveOwnAdminRole(CannotRemoveOwnAdminRoleException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse("cannot_remove_own_admin_role"));
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleApiKeyNotFoundException(ApiKeyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("api_key_not_found"));
    }

    @ExceptionHandler({UserNotActiveException.class, TenantNotActiveException.class})
    public ResponseEntity<ApiErrorResponse> handleNotActive(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse("invalid_credentials"));
    }

    @ExceptionHandler(SigningKeyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSigningKeyNotFoundException(SigningKeyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("signing_key_not_found"));
    }

    @ExceptionHandler(CannotRevokeActiveKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleCannotRevokeActiveKeyException(CannotRevokeActiveKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("cannot_revoke_active_key"));
    }

    @ExceptionHandler(CannotResetOwnPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleCannotResetOwnPasswordException(CannotResetOwnPasswordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("cannot_reset_own_password"));
    }

    @ExceptionHandler(UserLockedException.class)
    public ResponseEntity<ApiErrorResponse> handleUserLocked(UserLockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(new ApiErrorResponse(
            "user_locked", "conta bloqueada por excesso de tentativas", ex.getLockedUntil()));
    }
}
