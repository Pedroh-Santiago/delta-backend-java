package br.com.deltaglobalbank.identity.domain.ipAllowlist;

public abstract sealed class IpAllowlistExceptions extends RuntimeException
    permits InvalidCidrException, CidrAlreadyExistsException,
    IpAllowlistEntryNotFoundException, IpNotAllowedException {

    protected IpAllowlistExceptions(String message) {
        super(message);
    }
}
