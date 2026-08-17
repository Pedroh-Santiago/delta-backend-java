package br.com.deltaglobalbank.identity.domain.user

import br.com.deltaglobalbank.identity.domain.role.RoleCode

sealed class UserDomainException(message: String) : RuntimeException(message)

// Auth
class UserNotFound : UserDomainException("User not found")
class InvalidCredentialsException : UserDomainException("invalid_credentials")
class UserNotActiveException : UserDomainException("invalid_credentials")
class TenantNotActiveException : UserDomainException("invalid_credentials")

// Change password
class CurrentPasswordIncorrectException : UserDomainException("current_password_incorrect")
class NewPasswordSameAsCurrentException : UserDomainException("new_password_same_as_current")

class EmailAlreadyExistsException : UserDomainException("email_already_exists")
class TenantNotFoundException : UserDomainException("tenant_not_found")
class TenantInactiveException : UserDomainException("tenant_inactive")
class RoleNotFoundException(val roleCode: String) : UserDomainException("role_not_found")
class ModuleNotEnabledForTenantException(val moduleCode: String) : UserDomainException("module_not_enabled_for_tenant")
class TenantAccessDeniedException : UserDomainException("tenant_access_denied")
class DuplicateRoleException : UserDomainException("duplicate_role")
class CannotRemoveOwnAdminRoleException : UserDomainException("cannot_remove_own_admin_role")
