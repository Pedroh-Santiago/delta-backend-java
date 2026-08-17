package br.com.deltaglobalbank.identity.domain.ipAllowlist

sealed class IpAllowlistExceptions(message: String) : RuntimeException(message)

class InvalidCidrException : IpAllowlistExceptions("invalid_cidr")
class CidrAlreadyExistsException : IpAllowlistExceptions("cidr_already_exists")
class IpAllowlistEntryNotFoundException : IpAllowlistExceptions("ip_allowlist_entry_not_found")
class IpNotAllowedException : IpAllowlistExceptions("ip_not_allowed")