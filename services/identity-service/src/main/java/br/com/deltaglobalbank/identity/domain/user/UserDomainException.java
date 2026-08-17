package br.com.deltaglobalbank.identity.domain.user;

public abstract sealed class UserDomainException extends RuntimeException
    permits UserNotFound, InvalidCredentialsException, UserNotActiveException,
    TenantNotActiveException, CurrentPasswordIncorrectException, NewPasswordSameAsCurrentException,
    EmailAlreadyExistsException, TenantNotFoundException, TenantInactiveException,
    ModuleNotEnabledForTenantException, TenantAccessDeniedException,
    DuplicateRoleException, CannotRemoveOwnAdminRoleException, CannotResetOwnPasswordException,
    UserLockedException {

    protected UserDomainException(String message) {
        super(message);
    }
}
